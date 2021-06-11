package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.InnsendtSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;

@Component
public class InnsendtSoknadService {

    
    private HenvendelseService henvendelseService;

    private VedleggService vedleggService;

    private static final Predicate<Vedlegg> IKKE_KVITTERING = vedlegg -> !SKJEMANUMMER_KVITTERING.equalsIgnoreCase(vedlegg.getSkjemaNummer());
    private static final Predicate<Vedlegg> LASTET_OPP = v -> Vedlegg.Status.LastetOpp.equals(v.getInnsendingsvalg());
    private static final Predicate<Vedlegg> IKKE_LASTET_OPP = LASTET_OPP.negate();

    
    
    @Autowired
    public InnsendtSoknadService(HenvendelseService henvendelseService, VedleggService vedleggService) {
		super();
		this.henvendelseService = henvendelseService;
		this.vedleggService = vedleggService;
	}

	public InnsendtSoknad hentInnsendtSoknad(String behandlingsId, String sprak) {
        final XMLHenvendelse xmlHenvendelse = henvendelseService.hentInformasjonOmAvsluttetSoknad(behandlingsId);

        List<XMLMetadata> metadata = xmlHenvendelse.getMetadataListe().getMetadata();
        XMLHovedskjema hovedskjema = (XMLHovedskjema) metadata.stream()
                .filter(xmlMetadata -> xmlMetadata instanceof XMLHovedskjema)
                .findFirst()
                .orElseThrow(() -> new SendSoknadException(String.format("Soknaden %s har ikke noe hovedskjema", behandlingsId)));

        final Locale locale = LocaleUtils.toLocale(sprak);
        InnsendtSoknad innsendtSoknad = new InnsendtSoknad(locale);

        try {
            KravdialogInformasjon konfigurasjon = KravdialogInformasjonHolder.hentKonfigurasjon(hovedskjema.getSkjemanummer());
            String prefix = konfigurasjon.getSoknadTypePrefix();
            innsendtSoknad.medTittelCmsKey(prefix.concat(".").concat("skjema.tittel"));
        } catch (SendSoknadException e) {//NOSONAR
            /*Dersom vi får en ApplicationException betyr det at soknaden ikke har noen konfigurasjon i sendsoknad.
            * Det er mest sannsynlig fordi soknaden er sendt inn via dokumentinnsending. I dette tilfellet bruker vi tittelen
            * på hoveddokumentet som skjematittel. Denne finnes for alle soknader.
            * */
        }

        final List<Vedlegg> vedlegg = metadata.stream()
                .filter(xmlMetadata -> xmlMetadata instanceof XMLVedlegg)
                .map(xmlMetadata -> {
                    XMLVedlegg xmlVedlegg = (XMLVedlegg) xmlMetadata;
                    Vedlegg v = new Vedlegg()
                            .medInnsendingsvalg(Transformers.toInnsendingsvalg(xmlVedlegg.getInnsendingsvalg()))
                            .medSkjemaNummer(xmlVedlegg.getSkjemanummer())
                            .medSkjemanummerTillegg(xmlVedlegg.getSkjemanummerTillegg())
                            .medNavn(TilleggsInfoService.lesTittelFraJsonString(xmlVedlegg.getTilleggsinfo()));
                    vedleggService.medKodeverk(v);
                    return v;
                })
                .filter(IKKE_KVITTERING)
                .collect(toList());

        Optional<Vedlegg> hovedskjemaVedlegg = vedlegg.stream()
                .filter(medSkjemanummer(hovedskjema.getSkjemanummer())).findFirst();

        List<Vedlegg> innsendteVedlegg = vedlegg.stream().filter(LASTET_OPP).collect(toList());
        List<Vedlegg> ikkeInnsendteVedlegg = vedlegg.stream().filter(IKKE_LASTET_OPP).collect(toList());

        return innsendtSoknad
                .medTittel(hovedskjemaVedlegg.orElse(new Vedlegg()).getTittel())
                .medBehandlingId(xmlHenvendelse.getBehandlingsId())
                .medTemakode(xmlHenvendelse.getTema())
                .medInnsendteVedlegg(innsendteVedlegg)
                .medIkkeInnsendteVedlegg(ikkeInnsendteVedlegg)
                .medDato(xmlHenvendelse.getAvsluttetDato());
    }

    private Predicate<Vedlegg> medSkjemanummer(final String skjemanummer) {
        return vedlegg -> vedlegg.getSkjemaNummer().equalsIgnoreCase(skjemanummer);
    }
}
