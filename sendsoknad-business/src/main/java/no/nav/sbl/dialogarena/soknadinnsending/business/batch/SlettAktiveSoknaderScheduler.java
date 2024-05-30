package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static org.slf4j.LoggerFactory.getLogger;

@Service
@EnableSchedulerLock(defaultLockAtMostFor = "20m")
public class SlettAktiveSoknaderScheduler {

    private static final Logger logger = getLogger(GamleSoknaderSletterScheduler.class);

    // PROD
    // private static final String SCHEDULE_TIME = "0 10 1 JUN * ?"; // at 10 o'clock the 1. of june

    // DEV
    private static final String SCHEDULE_TIME = "30 15 30 MAY *"; // at 15:30 o'clock the 30. of may

    private static final int SLETT_GAMLE_SOKNADER_UNDER_ARBEID = 0; // Behold metadata, men slett vedlegg etc. for alle søknader under_arbeid

    private final SoknadRepository soknadRepository;
    private final SoknadService soknadService;

    @Autowired
    public SlettAktiveSoknaderScheduler(SoknadRepository soknadRepository, SoknadService soknadService) {
        this.soknadRepository = soknadRepository;
        this.soknadService = soknadService;
    }


    @Scheduled(cron = SCHEDULE_TIME)
    @SchedulerLock(name = "slettAktiveSoknader", lockAtLeastFor = "15m")
    public void slettGamleSoknader() {
        long startTime = System.currentTimeMillis();
        logger.info("Starter jobb for å slette alle aktive søknader");

        soknadService.automatiskSlettingAvSoknader(HendelseType.AVBRUTT_AUTOMATISK, false, SLETT_GAMLE_SOKNADER_UNDER_ARBEID);

        logger.debug(
                "Ferdig med å slette vedlegg etc. til ikke innsendte soknader opprettet for mer enn {} dager opprettet for mer enn {} dager. Tidsbruk: {}ms",
                SLETT_GAMLE_SOKNADER_UNDER_ARBEID,  (System.currentTimeMillis() - startTime));
    }

}
