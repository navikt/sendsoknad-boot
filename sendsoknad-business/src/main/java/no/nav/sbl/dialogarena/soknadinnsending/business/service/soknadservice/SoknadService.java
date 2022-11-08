package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.Brukernotifikasjon;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.SendesSenere;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadService {

    private static final Logger logger = getLogger(SoknadService.class);

    private final SoknadRepository lokalDb;
    private final EttersendingService ettersendingService;
    private final WebSoknadConfig config;
    private final SoknadDataFletter soknadDataFletter;
    private final SoknadMetricsService soknadMetricsService;
    private final Brukernotifikasjon brukernotifikasjon;


    @Autowired
    public SoknadService(
            @Qualifier("soknadInnsendingRepository") SoknadRepository lokalDb,
            EttersendingService ettersendingService, WebSoknadConfig config,
            SoknadDataFletter soknadDataFletter, SoknadMetricsService soknadMetricsService,
            Brukernotifikasjon brukernotifikasjon
    ) {
        this.lokalDb = lokalDb;
        this.ettersendingService = ettersendingService;
        this.config = config;
        this.soknadDataFletter = soknadDataFletter;
        this.soknadMetricsService = soknadMetricsService;
        this.brukernotifikasjon = brukernotifikasjon;
    }

    public void settDelsteg(String behandlingsId, DelstegStatus delstegStatus) {
        lokalDb.settDelstegstatus(behandlingsId, delstegStatus);
    }

    public void settJournalforendeEnhet(String behandlingsId, String journalforendeEnhet) {
        lokalDb.settJournalforendeEnhet(behandlingsId, journalforendeEnhet);
    }

    public WebSoknad hentSoknadFraLokalDb(long soknadId) {
        return lokalDb.hentSoknad(soknadId);
    }

    public SoknadStruktur hentSoknadStruktur(String skjemanummer) {
        return config.hentStruktur(skjemanummer);
    }

    public WebSoknad hentEttersendingForBehandlingskjedeId(String behandlingsId) {
        return lokalDb.hentEttersendingMedBehandlingskjedeId(behandlingsId).orElse(null);
    }

    @Transactional
    public String startSoknad(String skjemanummer, String aktorId) {
        return soknadDataFletter.startSoknad(skjemanummer, aktorId);
    }

    @Transactional
    public void avbrytSoknad(String behandlingsId) {
        WebSoknad soknad = lokalDb.hentSoknad(behandlingsId);

        /*
         * Sletter alle vedlegg til søknader som blir avbrutt.
         * Dette burde egentlig gjøres i henvendelse, siden vi uansett skal slette alle vedlegg på avbrutte søknader.
         * I tillegg blir det liggende igjen mange vedlegg for søknader som er avbrutt før dette kallet ble lagt til.
         */
        String brukerBehandlingId = soknad.getBrukerBehandlingId();
        logger.info("behandlingsId: {}, brukerBehandlingId: {}, BehandlingskjedeId: {}", behandlingsId, brukerBehandlingId, soknad.getBehandlingskjedeId());

        lokalDb.slettSoknad(soknad, HendelseType.AVBRUTT_AV_BRUKER);
        try {
            if (soknad.getVedlegg() != null && !soknad.getVedlegg().isEmpty()) {
                soknadDataFletter.deleteFiles(brukerBehandlingId, soknad.getVedlegg().stream()
                        .filter(v -> v.getStorrelse() > 0 && v.getFillagerReferanse() != null && Vedlegg.Status.LastetOpp.equals(v.getInnsendingsvalg()))
                        .map(Vedlegg::getFillagerReferanse).collect(Collectors.toList()));
            }
            String behandlingskjedeId = soknad.getBehandlingskjedeId() != null ? soknad.getBehandlingskjedeId() : behandlingsId;
            brukernotifikasjon.cancelNotification(brukerBehandlingId, behandlingskjedeId, soknad.erEttersending(), soknad.getAktoerId());
        } catch (Exception e) {
            logger.error("{}: Failed to cancel Brukernotifikasjon", brukerBehandlingId, e);
            throw e;
        }

        soknadMetricsService.avbruttSoknad(soknad.getskjemaNummer(), soknad.erEttersending());
    }

    public String startEttersending(String behandlingsIdSoknad, String aktorId) {
        return ettersendingService.start(behandlingsIdSoknad, aktorId);
    }

    public WebSoknad hentSoknad(String behandlingsId, boolean medData, boolean medVedlegg) {
        return soknadDataFletter.hentSoknad(behandlingsId, medData, medVedlegg);
    }

    public Long hentOpprinneligInnsendtDato(String behandlingsId) {
        return soknadDataFletter.hentOpprinneligInnsendtDato(behandlingsId);
    }

    public String hentSisteInnsendteBehandlingsId(String behandlingsId) {
        return soknadDataFletter.hentSisteInnsendteBehandlingsId(behandlingsId);
    }

    public void sendSoknad(String behandlingsId, byte[] pdf) {
        sendSoknad(behandlingsId, pdf, null);
    }

    public void sendSoknad(String behandlingsId, byte[] soknadPdf, byte[] fullSoknad) {
        WebSoknad soknad = soknadDataFletter.sendSoknad(behandlingsId, soknadPdf, fullSoknad);

        startEttersendingIfNeeded(soknad);
    }

    private void startEttersendingIfNeeded(WebSoknad soknad) {
        List<Vedlegg> paakrevdeVedlegg = soknad.getVedlegg().stream().filter(v -> v.getInnsendingsvalg().er(SendesSenere)).collect(Collectors.toList());
        if (!paakrevdeVedlegg.isEmpty()) {
            logger.info("{}: Soknad har vedlegg med Status {} og uten data. Starter ettersending.", soknad.getBrukerBehandlingId(), SendesSenere);
            String behandlingSkjedeID = soknad.getBehandlingskjedeId() != null ? soknad.getBehandlingskjedeId() : soknad.getBrukerBehandlingId();
            ettersendingService.start( behandlingSkjedeID, soknad.getAktoerId());
        }
    }
}
