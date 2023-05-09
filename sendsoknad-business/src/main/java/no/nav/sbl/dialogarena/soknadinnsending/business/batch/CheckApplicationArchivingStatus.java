package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.InnsendtSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadMetricsService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static org.slf4j.LoggerFactory.getLogger;

@Service
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class CheckApplicationArchivingStatus {

    private static final Logger logger = getLogger(CheckApplicationArchivingStatus.class);
    private static final String SCHEDULE_TIME = "0 0,11 0 ? * *"; // Every 11th minutes

    private static final int OFFSET_MINUTES = 95; // Maximum minutes before soknadsarkiverer is expected to use to archive an application.
    private final InnsendtSoknadService innsendtSoknadService;

    @Autowired
    public CheckApplicationArchivingStatus(InnsendtSoknadService innsendtSoknadService, SoknadMetricsService soknadMetricsService) {
        this.innsendtSoknadService = innsendtSoknadService;
     }

    @Scheduled(cron = SCHEDULE_TIME)
    @SchedulerLock(name = "checkApplicationArchivingStatus", lockAtLeastFor = "5m")
    public void slettDataForArkiverteSoknader() {
        if (Boolean.parseBoolean(System.getProperty("sendsoknad.batch.enabled", "true"))) {
            long startTime = System.currentTimeMillis();
            logger.debug("Starter jobb for å sjekke at innsendte søknader er blitt arkivert");

            innsendtSoknadService.checkArchivingStatusOfSentinApplications(OFFSET_MINUTES);

            logger.info("Ferdig sjekket status for arkiverte soknader. Prosesseringstid: {}ms", (System.currentTimeMillis() - startTime));
        } else {
            logger.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å aktivere sjekk av arkiveringsstatus til innsendte soknader");
        }
    }



}
