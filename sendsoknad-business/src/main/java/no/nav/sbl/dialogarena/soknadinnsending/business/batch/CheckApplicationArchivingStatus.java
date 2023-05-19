package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.InnsendtSoknadService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class CheckApplicationArchivingStatus {

    private static final Logger logger = getLogger(CheckApplicationArchivingStatus.class);
    private static final String SCHEDULE_TIME = "* /11 * * * *"; // Every 11th minutes

    private static final int OFFSET_MINUTES = 95; // Maximum minutes before soknadsarkiverer is expected to respond with archiving status.
    private final InnsendtSoknadService innsendtSoknadService;

    @Autowired
    public CheckApplicationArchivingStatus(InnsendtSoknadService innsendtSoknadService) {
        this.innsendtSoknadService = innsendtSoknadService;
    }

    @Scheduled(cron = SCHEDULE_TIME)
    @SchedulerLock(name = "checkApplicationArchivingStatus", lockAtLeastFor = "10m")
    public void slettDataForArkiverteSoknader() {
        long startTime = System.currentTimeMillis();
        logger.debug("Starter jobb for å sjekke at innsendte søknader er blitt arkivert");

        innsendtSoknadService.checkArchivingStatusOfSentinApplications(OFFSET_MINUTES);

        logger.info("Ferdig sjekket status for arkiverte soknader. Prosesseringstid: {}ms", (System.currentTimeMillis() - startTime));
    }



}