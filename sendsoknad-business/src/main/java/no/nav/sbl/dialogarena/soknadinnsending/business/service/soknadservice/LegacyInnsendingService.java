package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggFraHenvendelsePopulator;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.*;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.SENDES_IKKE;
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
    private final FillagerService fillagerService;
    private final AlternativRepresentasjonService alternativRepresentasjonService;
    private final TekstHenter tekstHenter;

    @Autowired
    public LegacyInnsendingService(HenvendelseService henvendelseService, SkjemaOppslagService skjemaOppslagService,
                                   FillagerService fillagerService, TekstHenter tekstHenter,
                                   AlternativRepresentasjonService alternativRepresentasjonService,
                                   VedleggFraHenvendelsePopulator vedleggFraHenvendelsePopulator) {
        this.henvendelseService = henvendelseService;
        this.vedleggFraHenvendelsePopulator = vedleggFraHenvendelsePopulator;
        this.skjemaOppslagService = skjemaOppslagService;
        this.fillagerService = fillagerService;
        this.alternativRepresentasjonService = alternativRepresentasjonService;
        this.tekstHenter = tekstHenter;
    }


    public void sendSoknad(WebSoknad soknad, byte[] pdf, byte[] fullSoknad) {
        XMLHovedskjema hovedskjema = lagXmlHovedskjemaMedAlternativRepresentasjon(pdf, soknad, fullSoknad);
        XMLVedlegg[] vedlegg = convertToXmlVedleggListe(vedleggFraHenvendelsePopulator.hentVedleggOgKvittering(soknad), skjemaOppslagService);

        XMLSoknadMetadata soknadMetadata = EkstraMetadataService.hentEkstraMetadata(soknad);
        henvendelseService.avsluttSoknad(soknad.getBrukerBehandlingId(), hovedskjema, vedlegg, soknadMetadata);
    }

    private XMLHovedskjema lagXmlHovedskjemaMedAlternativRepresentasjon(byte[] pdf, WebSoknad soknad, byte[] fullSoknad) {

        XMLHovedskjema hovedskjema = new XMLHovedskjema()
                .withInnsendingsvalg(LASTET_OPP.toString())
                .withSkjemanummer(skjemanummer(soknad))
                .withFilnavn(skjemanummer(soknad) + ".pdfa")
                .withMimetype("application/pdf")
                .withFilstorrelse("" + pdf.length)
                .withUuid(soknad.getUuid())
                .withTilleggsinfo(skjemaOppslagService.getTittel(soknad.getskjemaNummer()))
                .withJournalforendeEnhet(journalforendeEnhet(soknad));

        if (!soknad.erEttersending()) {
            XMLAlternativRepresentasjonListe xmlAlternativRepresentasjonListe = new XMLAlternativRepresentasjonListe();
            hovedskjema = hovedskjema.withAlternativRepresentasjonListe(
                    xmlAlternativRepresentasjonListe
                            .withAlternativRepresentasjon(lagListeMedXMLAlternativeRepresentasjoner(soknad)));
            if (fullSoknad != null) {
                XMLAlternativRepresentasjon fullSoknadRepr = new XMLAlternativRepresentasjon()
                        .withUuid(UUID.randomUUID().toString())
                        .withFilnavn(skjemanummer(soknad) + ".pdfa")
                        .withMimetype("application/pdf-fullversjon")
                        .withFilstorrelse("" + fullSoknad.length);
                fillagerService.lagreFil(soknad.getBrukerBehandlingId(), fullSoknadRepr.getUuid(), soknad.getAktoerId(), new ByteArrayInputStream(fullSoknad));
                xmlAlternativRepresentasjonListe.withAlternativRepresentasjon(fullSoknadRepr);
            }
        }

        return hovedskjema;
    }

    private List<XMLAlternativRepresentasjon> lagListeMedXMLAlternativeRepresentasjoner(WebSoknad soknad) {
        List<AlternativRepresentasjon> alternativeRepresentasjoner = alternativRepresentasjonService.hentAlternativeRepresentasjoner(soknad, tekstHenter);
        alternativRepresentasjonService.lagreTilFillager(soknad.getBrukerBehandlingId(), soknad.getAktoerId(), alternativeRepresentasjoner);
        return alternativRepresentasjonService.lagXmlFormat(alternativeRepresentasjoner);
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
