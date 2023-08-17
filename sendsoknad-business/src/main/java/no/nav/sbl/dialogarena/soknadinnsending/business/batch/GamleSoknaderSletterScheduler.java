package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@Service
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class GamleSoknaderSletterScheduler {

    private static final Logger logger = getLogger(GamleSoknaderSletterScheduler.class);
    private static final String SCHEDULE_TIME = "0 36 14 * * ?"; // Every day at 04 in the morning
    private static final int SLETT_GAMLE_SOKNADER_UNDER_ARBEID = 7*8; // Behold metadata, men slett vedlegg etc. 8 uker etter opprettelse
    private static final int SLETT_GAMLE_SOKNADER_PERMANENT = 7*26;  // Slett alle søknader etter et halvt år etter opprettelse
    private final SoknadRepository soknadRepository;
    private final SoknadService soknadService;


    @Autowired
    public GamleSoknaderSletterScheduler(SoknadRepository soknadRepository, SoknadService soknadService) {
        this.soknadRepository = soknadRepository;
        this.soknadService = soknadService;
    }

    @Scheduled(cron = SCHEDULE_TIME)
    @SchedulerLock(name = "slettGamleSoknader", lockAtLeastFor = "5m")
    public void slettGamleSoknader() {
        if (Boolean.parseBoolean(System.getProperty("sendsoknad.batch.enabled", "true"))) {
            long startTime = System.currentTimeMillis();
            logger.info("Starter jobb for å slette gamle soknader");

            soknadService.automatiskSlettingAvSoknader(HendelseType.AVBRUTT_AUTOMATISK, false, SLETT_GAMLE_SOKNADER_UNDER_ARBEID);

            soknadService.automatiskSlettingAvSoknader(HendelseType.PERMANENT_SLETTET_AV_SYSTEM, false, SLETT_GAMLE_SOKNADER_PERMANENT);

            logger.debug(
                    "Ferdig med å slette vedlegg etc. til ikke innsendte soknader opprettet for mer enn {} dager og permanent sletting av søknader opprettet for mer enn {} dager. Tidsbruk: {}ms",
                    SLETT_GAMLE_SOKNADER_UNDER_ARBEID, SLETT_GAMLE_SOKNADER_PERMANENT, (System.currentTimeMillis() - startTime));
        } else {
            logger.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å slette gamle soknader");
        }
    }
}