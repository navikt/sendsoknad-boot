package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.InnsendtSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.apache.commons.lang3.LocaleUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class InnsendtSoknadService {

    private static final Logger logger = getLogger(InnsendtSoknadService.class);

    private final VedleggService vedleggService;

    private final SoknadRepository lokalDb;


    @Autowired
    public InnsendtSoknadService(VedleggService vedleggService, @Qualifier("soknadInnsendingRepository") SoknadRepository lokalDb) {
        this.vedleggService = vedleggService;
        this.lokalDb = lokalDb;
    }

    public InnsendtSoknad hentInnsendtSoknad(String behandlingsId, String sprak) {
        WebSoknad webSoknad = lokalDb.hentSoknadMedVedlegg(behandlingsId);
        vedleggService.leggTilKodeverkFelter(webSoknad.getVedlegg());

        if (webSoknad.getInnsendteVedlegg().isEmpty()) {
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

        List<Vedlegg> vedlegg = webSoknad.getVedlegg().stream()
                .filter(v -> !SKJEMANUMMER_KVITTERING.equalsIgnoreCase(v.getSkjemaNummer()))
                .map(m -> new Vedlegg()
                        .medInnsendingsvalg(m.getInnsendingsvalg())
                        .medSkjemaNummer(m.getSkjemaNummer())
                        .medSkjemanummerTillegg(m.getSkjemanummerTillegg())
                        .medTittel((m.getTittel() == null || m.getTittel().isEmpty() ? SkjemaOppslagService.getTittel(m.getSkjemaNummer()) : m.getTittel()))
                        .medNavn(m.getNavn() == null || m.getNavn().isEmpty() ? SkjemaOppslagService.getTittel(m.getSkjemaNummer()) : m.getNavn())
                )
                .peek(v -> {
                    if (erLastetOpp(v))
                        logger.info("{}: hentInnsendtSoknad: skjemanr={} navn={} skjemanummerTillegg={}",
                                webSoknad.getBrukerBehandlingId(), v.getSkjemaNummer(), v.getNavn(), v.getSkjemanummerTillegg());
                })
                .collect(toList());

        return innsendtSoknad
                .medTittel(getTittel(webSoknad))
                .medTemakode(getTema(webSoknad, konfigurasjon))
                .medInnsendteVedlegg(vedlegg.stream().filter(this::erLastetOpp).collect(toList()))
                .medIkkeInnsendteVedlegg(vedlegg.stream().filter(v -> !erLastetOpp(v)).collect(toList()))
                .medBehandlingId(behandlingsId)
                .medDato(webSoknad.getInnsendtDato());
    }

    @NotNull
    private String getTittel(WebSoknad webSoknad) {
        return webSoknad.getVedlegg().stream()
                .filter(v -> webSoknad.getskjemaNummer().equalsIgnoreCase(v.getSkjemaNummer()))
                .map(Vedlegg::getTittel)
                .findFirst()
                .orElse("");
    }

    private String getTema(WebSoknad webSoknad, KravdialogInformasjon konfigurasjon) {
        if (konfigurasjon != null && konfigurasjon.getTema() != null && !konfigurasjon.getTema().isEmpty())
            return konfigurasjon.getTema();
        else
            return SkjemaOppslagService.getTema(webSoknad.getskjemaNummer());
    }

    private boolean erLastetOpp(Vedlegg v) {
        return Vedlegg.Status.LastetOpp.equals(v.getInnsendingsvalg());
    }
}
