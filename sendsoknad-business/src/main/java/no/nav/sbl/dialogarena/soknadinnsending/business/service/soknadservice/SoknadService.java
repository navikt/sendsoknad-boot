package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
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
    public void avbrytSoknad(String brukerBehandlingId) {
        WebSoknad soknad = lokalDb.hentSoknad(brukerBehandlingId);
        logger.info("{}: Avbryter soknad med BehandlingskjedeId: {}", brukerBehandlingId, soknad.getBehandlingskjedeId());

        lokalDb.slettSoknadPermanent(soknad.getSoknadId(), HendelseType.PERMANENT_SLETTET_AV_BRUKER);

        slettFilerOgKanselerBrukerNotifikasjon(soknad);

        soknadMetricsService.avbruttSoknad(soknad.getskjemaNummer(), soknad.erEttersending());
    }

    public void automatiskSlettingAvSoknader(HendelseType hendelseType, boolean kunArkiverte, int dager) {
        List<WebSoknad> slettedeSoknader;
        if (hendelseType == HendelseType.AVBRUTT_AUTOMATISK) {
            slettedeSoknader =lokalDb.slettGamleIkkeInnsendteSoknader(dager);
        } else {
            if (kunArkiverte) {
                lokalDb.finnOgSlettDataTilArkiverteSoknader(dager);
                return;
            }
            slettedeSoknader = lokalDb.slettGamleSoknaderPermanent(dager);
        }
        slettedeSoknader.forEach(soknad -> slettFilerOgKanselerBrukerNotifikasjon(soknad));
    }

    public void slettFilerOgKanselerBrukerNotifikasjon(WebSoknad soknad) {
        logger.info("{}: Fjerner filer og eventuell brukernotifikasjon etter sletting søknad {}", soknad.getBrukerBehandlingId(), soknad.getSoknadId());
        List<String> fileids = soknad.getVedlegg().stream()
                .filter(v -> v.getStorrelse() > 0 && v.getFillagerReferanse() != null && Vedlegg.Status.LastetOpp.equals(v.getInnsendingsvalg()))
                .map(Vedlegg::getFillagerReferanse)
                .collect(Collectors.toList());
        if (!fileids.isEmpty())
            soknadDataFletter.deleteFiles(soknad.getBrukerBehandlingId(), fileids);

        if (soknad.getStatus() == SoknadInnsendingStatus.UNDER_ARBEID) {
            try {
                String behandlingskjedeId = soknad.getBehandlingskjedeId() != null ? soknad.getBehandlingskjedeId() : soknad.getBrukerBehandlingId();
                brukernotifikasjon.cancelNotification(soknad.getBrukerBehandlingId(), behandlingskjedeId, soknad.erEttersending(), soknad.getAktoerId());

            } catch (Exception e) {
                logger.error("{}: Failed to cancel Brukernotifikasjon", soknad.getBrukerBehandlingId(), e);
                throw e;
            }
        }
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
        boolean harPaakrevdeVedlegg = soknad.getVedlegg().stream().anyMatch(v -> v.getInnsendingsvalg().er(SendesSenere));
        if (harPaakrevdeVedlegg) {
            String behandlingskjedeId = soknad.getBehandlingskjedeId() != null ? soknad.getBehandlingskjedeId() : soknad.getBrukerBehandlingId();
            logger.info("{}: Soknad har vedlegg med Status {} og uten data. Starter ettersending på id {}.",
                    soknad.getBrukerBehandlingId(), SendesSenere, behandlingskjedeId);

            String ettersendelseId = ettersendingService.start(soknad, behandlingskjedeId, soknad.getAktoerId(), true);

            logger.info("{}: Soknad med BehandlingsId {} har fått ettersending opprettet med BehandlingsId {}",
                    behandlingskjedeId, behandlingskjedeId, ettersendelseId);
        }
    }
}