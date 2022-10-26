package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHenvendelse;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.InnsendtSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.toInnsendingsvalg;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;

@Component
public class InnsendtSoknadService {

    private final HenvendelseService henvendelseService;
    private final VedleggService vedleggService;

    private final SoknadRepository lokalDb;

    private static final Predicate<Vedlegg> IKKE_KVITTERING = vedlegg -> !SKJEMANUMMER_KVITTERING.equalsIgnoreCase(vedlegg.getSkjemaNummer());
    private static final Predicate<Vedlegg> LASTET_OPP = v -> Vedlegg.Status.LastetOpp.equals(v.getInnsendingsvalg());
    private static final Predicate<Vedlegg> IKKE_LASTET_OPP = LASTET_OPP.negate();


    @Autowired
    public InnsendtSoknadService(HenvendelseService henvendelseService, VedleggService vedleggService, @Qualifier("soknadInnsendingRepository") SoknadRepository lokalDb) {
        super();
        this.henvendelseService = henvendelseService;
        this.vedleggService = vedleggService;
        this.lokalDb = lokalDb;
    }

    public InnsendtSoknad hentInnsendtSoknad(String behandlingsId, String sprak) {
        if (SoknadDataFletter.GCP_ARKIVERING_ENABLED) {
            WebSoknad webSoknad = lokalDb.hentSoknadMedVedlegg(behandlingsId);
            vedleggService.leggTilKodeverkFelter(webSoknad.hentPaakrevdeVedlegg());

            if (webSoknad == null || webSoknad.getInnsendteVedlegg().isEmpty()) {
                throw new SendSoknadException(String.format("Soknaden %s har ikke noe hovedskjema", behandlingsId));
            }
            final Locale locale = LocaleUtils.toLocale(sprak);
            InnsendtSoknad innsendtSoknad = new InnsendtSoknad(locale);
            KravdialogInformasjon konfigurasjon = null;
            try {
                konfigurasjon = KravdialogInformasjonHolder.hentKonfigurasjon(webSoknad.getskjemaNummer());
                String prefix = konfigurasjon.getSoknadTypePrefix();
                innsendtSoknad.medTittelCmsKey(prefix.concat(".").concat("skjema.tittel"));
            } catch (SendSoknadException e) {//NOSONAR
                /*Dersom vi får en ApplicationException betyr det at soknaden ikke har noen konfigurasjon i sendsoknad.
                 * Det er mest sannsynlig fordi soknaden er sendt inn via dokumentinnsending. I dette tilfellet bruker vi tittelen
                 * på hoveddokumentet som skjematittel. Denne finnes for alle soknader.
                 * */
            }

            Optional<Vedlegg> hovedskjema = webSoknad.getVedlegg().stream().filter(v-> webSoknad.getskjemaNummer().equalsIgnoreCase(v.getSkjemaNummer())).findFirst();
            List<Vedlegg> innsendteVedlegg = webSoknad.getVedlegg().stream()
                    .filter(v-> Vedlegg.Status.LastetOpp.equals(v.getInnsendingsvalg()))
                    .filter(v-> !SKJEMANUMMER_KVITTERING.equalsIgnoreCase(v.getSkjemaNummer()))
                    .collect(toList());
            List<Vedlegg> ikkeInnsendteVedlegg = webSoknad.getVedlegg().stream()
                    .filter(v-> !Vedlegg.Status.LastetOpp.equals(v.getInnsendingsvalg()) )
                    .collect(toList());
            return innsendtSoknad
                    .medTittel(hovedskjema.orElse(new Vedlegg()).getTittel())
                    .medBehandlingId(behandlingsId)
                    .medTemakode(konfigurasjon != null ? konfigurasjon.getTema(webSoknad.getskjemaNummer()) : "")
                    .medInnsendteVedlegg(innsendteVedlegg)
                    .medIkkeInnsendteVedlegg(ikkeInnsendteVedlegg)
                    .medDato(webSoknad.getInnsendtDato());

        } else {
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
                                .medInnsendingsvalg(toInnsendingsvalg(xmlVedlegg.getInnsendingsvalg()))
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
    }

    private Predicate<Vedlegg> medSkjemanummer(final String skjemanummer) {
        return vedlegg -> vedlegg.getSkjemaNummer().equalsIgnoreCase(skjemanummer);
    }
}
