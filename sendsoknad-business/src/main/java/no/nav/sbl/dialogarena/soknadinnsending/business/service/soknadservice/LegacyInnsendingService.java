package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggFraHenvendelsePopulator;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.journalforendeEnhet;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.skjemanummer;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class LegacyInnsendingService {
    private static final Logger logger = getLogger(LegacyInnsendingService.class);

    private final HenvendelseService henvendelseService;
    private final VedleggFraHenvendelsePopulator vedleggFraHenvendelsePopulator;
    private final SkjemaOppslagService skjemaOppslagService;

    @Autowired
    public LegacyInnsendingService(HenvendelseService henvendelseService, SkjemaOppslagService skjemaOppslagService,
                                   VedleggFraHenvendelsePopulator vedleggFraHenvendelsePopulator) {
        this.henvendelseService = henvendelseService;
        this.vedleggFraHenvendelsePopulator = vedleggFraHenvendelsePopulator;
        this.skjemaOppslagService = skjemaOppslagService;
    }


    public void sendSoknad(
            WebSoknad soknad,
            List<AlternativRepresentasjon> xmlAlternativRepresentasjoner,
            byte[] pdf,
            byte[] fullSoknad,
            String fullSoknadId
    ) {
        String tittel = skjemaOppslagService.getTittel(soknad.getskjemaNummer());
        XMLHovedskjema hovedskjema = lagXmlHovedskjemaMedAlternativRepresentasjon(pdf, soknad, fullSoknad, fullSoknadId, tittel, xmlAlternativRepresentasjoner);
        XMLVedlegg[] vedlegg = convertToXmlVedleggListe(vedleggFraHenvendelsePopulator.hentVedleggOgKvittering(soknad), skjemaOppslagService);
        logVedlegg(soknad, vedlegg);

        XMLSoknadMetadata soknadMetadata = EkstraMetadataService.hentEkstraMetadata(soknad);
        henvendelseService.avsluttSoknad(soknad.getBrukerBehandlingId(), hovedskjema, vedlegg, soknadMetadata);
    }

    private static XMLHovedskjema lagXmlHovedskjemaMedAlternativRepresentasjon(
            byte[] pdf,
            WebSoknad soknad,
            byte[] fullSoknad,
            String fullSoknadId,
            String tittel,
            List<AlternativRepresentasjon> alternativeRepresentations) {

        XMLHovedskjema hovedskjema = new XMLHovedskjema()
                .withInnsendingsvalg(LASTET_OPP.toString())
                .withSkjemanummer(skjemanummer(soknad))
                .withFilnavn(skjemanummer(soknad) + ".pdfa")
                .withMimetype("application/pdf")
                .withFilstorrelse("" + pdf.length)
                .withUuid(soknad.getUuid())
                .withTilleggsinfo(tittel)
                .withJournalforendeEnhet(journalforendeEnhet(soknad));

        if (!soknad.erEttersending()) {
            XMLAlternativRepresentasjonListe xmlAlternativRepresentasjonListe = new XMLAlternativRepresentasjonListe();
            hovedskjema = hovedskjema.withAlternativRepresentasjonListe(
                    xmlAlternativRepresentasjonListe
                            .withAlternativRepresentasjon(lagXmlFormat(alternativeRepresentations)));
            if (fullSoknad != null) {
                XMLAlternativRepresentasjon fullSoknadRepr = new XMLAlternativRepresentasjon()
                        .withUuid(fullSoknadId)
                        .withFilnavn(skjemanummer(soknad) + ".pdfa")
                        .withMimetype("application/pdf-fullversjon")
                        .withFilstorrelse("" + fullSoknad.length);
                xmlAlternativRepresentasjonListe.withAlternativRepresentasjon(fullSoknadRepr);
            }
        }

        return hovedskjema;
    }

    private static List<XMLAlternativRepresentasjon> lagXmlFormat(List<AlternativRepresentasjon> alternativeRepresentasjoner) {
        return alternativeRepresentasjoner.stream().map(r ->
                        new XMLAlternativRepresentasjon()
                                .withFilnavn(r.getFilnavn())
                                .withFilstorrelse(r.getContent().length + "")
                                .withMimetype(r.getMimetype())
                                .withUuid(r.getUuid()))
                .collect(toList());
    }

    private static XMLVedlegg[] convertToXmlVedleggListe(List<Vedlegg> vedleggForventnings, SkjemaOppslagService skjemaOppslagService) {
        List<XMLVedlegg> resultat = new ArrayList<>();
        for (Vedlegg vedlegg : vedleggForventnings) {
            XMLVedlegg xmlVedlegg;
            if (vedlegg.getInnsendingsvalg().er(LastetOpp)) {
                xmlVedlegg = new XMLVedlegg()
                        .withFilnavn(vedlegg.lagFilNavn())
                        .withSideantall(vedlegg.getAntallSider())
                        .withMimetype(isEmpty(vedlegg.getMimetype()) ? "application/pdf" : vedlegg.getMimetype())
                        .withTilleggsinfo(finnVedleggsnavn(vedlegg, skjemaOppslagService))
                        .withFilstorrelse(vedlegg.getStorrelse().toString())
                        .withSkjemanummer(vedlegg.getSkjemaNummer())
                        .withUuid(vedlegg.getFillagerReferanse())
                        .withInnsendingsvalg(LASTET_OPP.value());
            } else {
                xmlVedlegg = new XMLVedlegg()
                        .withFilnavn(vedlegg.lagFilNavn())
                        .withTilleggsinfo(vedlegg.getNavn())
                        .withSkjemanummer(vedlegg.getSkjemaNummer())
                        .withInnsendingsvalg(toXmlInnsendingsvalg(vedlegg.getInnsendingsvalg()));
            }
            String skjemanummerTillegg = vedlegg.getSkjemanummerTillegg();
            if (isNotBlank(skjemanummerTillegg)) {
                xmlVedlegg.setSkjemanummerTillegg(skjemanummerTillegg);
            }
            resultat.add(xmlVedlegg);
        }
        return resultat.toArray(new XMLVedlegg[0]);
    }

    private void logVedlegg(WebSoknad soknad, XMLVedlegg[] vedlegg) {
        for (int i = 0; i < vedlegg.length; i++) {
            boolean alleredeInnsendt = alleredeInnsendtSoknadsVedlegg(
                    soknad.erEttersending(),
                    "LASTET_OPP".equals(vedlegg[i].getInnsendingsvalg()),
                    vedlegg[i].getFilstorrelse()
            );

            logger.info("{}: Vedlegg {}/{} | Id: {}, Skjemanummer: {}, Filnavn: {}, Innsendingsvalg: {}, " +
                            "Filstorrelse: {} | erEttersending: {}, alleredeInnsendtSoknadsVedlegg(): {}",
                    soknad.getBrukerBehandlingId(), i + 1, vedlegg.length, vedlegg[i].getUuid(),
                    vedlegg[i].getSkjemanummer(), vedlegg[i].getFilnavn(), vedlegg[i].getInnsendingsvalg(),
                    vedlegg[i].getFilstorrelse(), soknad.erEttersending(), alleredeInnsendt);
        }
    }

    private boolean alleredeInnsendtSoknadsVedlegg(boolean erEttersendelse, boolean erLastetOpp, String filstorrelse) {
        return !(erEttersendelse && erLastetOpp && "0".equals(filstorrelse));
    }


    private static String finnVedleggsnavn(Vedlegg vedlegg, SkjemaOppslagService skjemaOppslagService) {
        if ("N6".equalsIgnoreCase(vedlegg.getSkjemaNummer()) && vedlegg.getNavn() != null && !"".equals(vedlegg.getNavn())) {
            return vedlegg.getNavn();
        }
        String skjemanummerTillegg = "";
        if (vedlegg.getSkjemanummerTillegg() != null && !"".equals(vedlegg.getSkjemanummerTillegg())) {
            skjemanummerTillegg = ": " + vedlegg.getSkjemanummerTillegg();
        }

        try {
            String skjemaNavn = skjemaOppslagService.getTittel(vedlegg.getSkjemaNummer());
            return skjemaNavn + skjemanummerTillegg;

        } catch (Exception e) {
            logger.warn("Unable to find tittel for '{}'", vedlegg.getSkjemaNummer());
            return null;
        }
    }

    private static String toXmlInnsendingsvalg(Vedlegg.Status innsendingsvalg) {
        switch (innsendingsvalg) {
            case LastetOpp:
                return LASTET_OPP.toString();
            case SendesSenere:
                return SEND_SENERE.toString();
            case VedleggSendesAvAndre:
                return VEDLEGG_SENDES_AV_ANDRE.toString();
            case VedleggSendesIkke:
                return VEDLEGG_SENDES_IKKE.toString();
            case VedleggAlleredeSendt:
                return VEDLEGG_ALLEREDE_SENDT.toString();
            case SendesIkke:
            default:
                return SENDES_IKKE.toString();
        }
    }
}
