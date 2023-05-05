package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static org.slf4j.LoggerFactory.getLogger;

@Service
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class SlettFilerTilInnsendteSoknader {

    private static final Logger logger = getLogger(GamleSoknaderSletterScheduler.class);
    private static final String SCHEDULE_TIME = "0 0 5 * * ?"; // Every day at 05 in the morning

    private static final int DAYS = 1; // Number of days until temporary stored attachments are deleted after successful archiving of an application.
    private final SoknadRepository soknadRepository;

    @Autowired
    public SlettFilerTilInnsendteSoknader(SoknadRepository soknadRepository) {
        this.soknadRepository = soknadRepository;
    }

    @Scheduled(cron = SCHEDULE_TIME)
    @SchedulerLock(name = "slettFilerTilInnsendteSoknader", lockAtLeastFor = "5m")
    public void slettDataForArkiverteSoknader() {
        if (Boolean.parseBoolean(System.getProperty("sendsoknad.batch.enabled", "true"))) {
            long startTime = System.currentTimeMillis();
            logger.debug("Starter jobb for å slette filer etc. for innsendte og arkiverte soknader");

            soknadRepository.finnOgSlettDataTilArkiverteSoknader(DAYS);

            logger.debug("Ferdig slettet søknader arkivert for mer enn {} dager siden. Prosesseringstid: {}ms",
                    DAYS,  (System.currentTimeMillis() - startTime));
        } else {
            logger.warn("Batch disabled. Må sette environment property sendsoknad.batch.enabled til true for å slette data til arkiverte soknader");
        }
    }

}
