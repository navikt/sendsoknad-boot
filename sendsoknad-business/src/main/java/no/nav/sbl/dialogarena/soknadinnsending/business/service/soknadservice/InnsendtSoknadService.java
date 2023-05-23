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

import java.time.LocalDateTime;
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
    private final SoknadMetricsService soknadMetricsService;


    @Autowired
    public InnsendtSoknadService(
            SoknadMetricsService soknadMetricsService,
            VedleggService vedleggService, @Qualifier("soknadInnsendingRepository") SoknadRepository lokalDb) {
        this.soknadMetricsService = soknadMetricsService;
        this.vedleggService = vedleggService;
        this.lokalDb = lokalDb;
    }

    public InnsendtSoknad hentInnsendtSoknad(String behandlingsId, String sprak) {
        WebSoknad soknad = lokalDb.hentSoknadMedVedlegg(behandlingsId);
        vedleggService.leggTilKodeverkFelter(soknad.getVedlegg());

        final Locale locale = LocaleUtils.toLocale(sprak);
        InnsendtSoknad innsendtSoknad = new InnsendtSoknad(locale);
        KravdialogInformasjon konfigurasjon = null;
        try {
            konfigurasjon = KravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer());
            String prefix = konfigurasjon.getSoknadTypePrefix();
            innsendtSoknad.medTittelCmsKey(prefix.concat(".skjema.tittel"));
        } catch (SendSoknadException e) {//NOSONAR
            /*Dersom vi får en ApplicationException betyr det at soknaden ikke har noen konfigurasjon i sendsoknad.
             * Det er mest sannsynlig fordi soknaden er sendt inn via dokumentinnsending. I dette tilfellet bruker vi tittelen
             * på hoveddokumentet som skjematittel. Denne finnes for alle soknader.
             * */
        }

        List<Vedlegg> vedlegg = soknad.getVedlegg().stream()
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
                                soknad.getBrukerBehandlingId(), v.getSkjemaNummer(), v.getNavn(), v.getSkjemanummerTillegg());
                })
                .collect(toList());

        return innsendtSoknad
                .medTittel(getTittel(soknad))
                .medTemakode(getTema(soknad, konfigurasjon))
                .medInnsendteVedlegg(vedlegg.stream().filter(this::erLastetOpp).collect(toList()))
                .medIkkeInnsendteVedlegg(vedlegg.stream().filter(v -> !erLastetOpp(v)).collect(toList()))
                .medBehandlingId(behandlingsId)
                .medDato(soknad.getInnsendtDato());
    }

    public void checkArchivingStatusOfSentinApplications(long offset_minutes) {
        int noOfAbsentInArchive = lokalDb.countInnsendtIkkeBehandlet(LocalDateTime.now().minusMinutes(offset_minutes));
        if (noOfAbsentInArchive > 0) {
            logger.error("Total number of applications not yet processed for archiving by soknadsarkiverer: " + noOfAbsentInArchive);
        }
        soknadMetricsService.arkiveringsRespons(noOfAbsentInArchive);
        soknadMetricsService.arkiveringsFeil(lokalDb.countArkiveringFeilet());
    }


    @NotNull
    private String getTittel(WebSoknad webSoknad) {
        return webSoknad.getVedlegg().stream()
                .filter(v -> webSoknad.getskjemaNummer().equalsIgnoreCase(v.getSkjemaNummer()))
                .map(Vedlegg::getTittel)
                .findFirst()
                .orElse(SkjemaOppslagService.getTittel(webSoknad.getskjemaNummer()));
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