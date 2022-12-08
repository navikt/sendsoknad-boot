package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.modig.common.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.EttersendingService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.FERDIG;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@EnableSchedulerLock(defaultLockAtMostFor = "30m")
public class HenvendelseImporter {

    private static final Logger logger = getLogger(HenvendelseImporter.class);
    private static final String SCHEDULE_TIME = "0 00 22 * * ?"; // At 22:00 every day
    private static final Boolean ER_INNSENDTE_SOKNADER_MED_MANGLENDE_VEDLEGG = true;

    private final SoknadDataFletter soknadDataFletter;
    private final EttersendingService ettersendingService;
    private final SoknadRepository lokalDb;

    private final String FILE;


    @Autowired
    public HenvendelseImporter(
            SoknadDataFletter soknadDataFletter,
            EttersendingService ettersendingService,
            SoknadRepository lokalDb,
            @Value("${henvendelsemigrering.file}") String file
    ) {
        this.soknadDataFletter = soknadDataFletter;
        this.ettersendingService = ettersendingService;
        this.lokalDb = lokalDb;
        this.FILE = file;
        logger.info("Henvendelse migration - file: {}", file);
    }

    @Scheduled(cron = SCHEDULE_TIME)
    @SchedulerLock(name = "slettGamleSoknader", lockAtLeastFor = "15m")
    public void migrateFromHenvendelse() {
        long startTime = System.currentTimeMillis();
        try {
            MDCOperations.putToMDC(MDCOperations.MDC_CALL_ID, MDCOperations.generateCallId());

            Map<String, String> idsAndTimestamps = getBehandlingsIdsToMigrate();
            long numberOfSoknaderThatWerePersisted = idsAndTimestamps.entrySet().stream()
                    .map(idAndTimestamp -> updateTimestampsInLocalDb(idAndTimestamp.getKey(), idAndTimestamp.getValue()))
                    .filter(persisted -> persisted)
                    .count();
            logger.info("Migrated {} soknader of {} to local database", numberOfSoknaderThatWerePersisted, idsAndTimestamps.size());

        } catch (Exception e) {
            logger.error("Migrating from Henvendelse failed!", e);
        }
        logger.info("Done migrating in {}ms", System.currentTimeMillis() - startTime);
    }

    private Map<String, String> getBehandlingsIdsToMigrate() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream is = classloader.getResourceAsStream(FILE);
             InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            Map<String, String> ids = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] idAndTimestamp = line.split("\t");
                if (idAndTimestamp.length >= 2 && !"".equals(idAndTimestamp[0])) {
                    ids.put(idAndTimestamp[0], idAndTimestamp[1]);
                }
            }
            logger.info("Found {} Soknader to migrate from file", ids.size());
            return ids;

        } catch (Exception e) {
            logger.error("Unable to read local file with migration ids");
            return Collections.emptyMap();
        }
    }


    private boolean updateTimestampsInLocalDb(String behandlingsId, String innsendtDato) {
        boolean persisted = false;
        long startTime = System.currentTimeMillis();
        try {
            WebSoknad soknad = lokalDb.hentSoknad(behandlingsId);
            if (soknad != null && soknad.getInnsendtDato() == null) {
                logger.info("{}: About to update timestamp on Soknad", behandlingsId);

                long innsendt = convertToDateTime(innsendtDato).getMillis();
                lokalDb.updateInnsendtDato(behandlingsId, innsendt);
                persisted = true;
            }
        } catch (Exception e) {
            logger.error("{}: Failed to update timestamp on Soknad. Time taken: {}ms",
                    behandlingsId, System.currentTimeMillis() - startTime , e);
        }
        return persisted;
    }

    private boolean persistInLocalDb(String behandlingsId, String innsendtDato) {
        boolean persisted = false;
        long startTime = System.currentTimeMillis();
        try {
            if (lokalDb.hentSoknad(behandlingsId) == null) {
                logger.info("{}: About to fetch and persist Soknad in local database", behandlingsId);

                WebSoknad soknad;
                if (ER_INNSENDTE_SOKNADER_MED_MANGLENDE_VEDLEGG) {
                    String aktor = lokalDb.findAktorIdFromHenvendelseMigration(behandlingsId);
                    if (aktor == null) {
                        logger.error("{}: Unable to find aktor mapping", behandlingsId);
                        return false;
                    }

                    // Will fetch and save to local database:
                    DateTime innsendt = convertToDateTime(innsendtDato);
                    soknad = ettersendingService.henvendelseMigrering(behandlingsId, aktor, innsendt);
                } else {
                    // Will fetch and save to local database:
                    soknad = soknadDataFletter.hentFraHenvendelse(behandlingsId, true);
                }

                persisted = soknad != null;
                if (soknad.getStatus() == UNDER_ARBEID || soknad.getStatus() == FERDIG) {
                    logger.info("{}: Done fetching and persisting Soknad with status {} in local database in {}ms.",
                            behandlingsId, soknad.getStatus(), System.currentTimeMillis() - startTime);
                } else {
                    logger.error("{}: Soknad had status {}, not {} or {} - did not persist. Time taken: {}ms.",
                            behandlingsId, soknad.getStatus(), UNDER_ARBEID, FERDIG,
                            System.currentTimeMillis() - startTime);
                    persisted = false;
                }

            } else {
                logger.info("{}: Soknad is already in local database", behandlingsId);
            }
        } catch (Exception e) {
            logger.error("{}: Failed to fetch and persist Soknad. Time taken: {}ms",
                    behandlingsId, System.currentTimeMillis() - startTime , e);
        }
        return persisted;
    }

    private DateTime convertToDateTime(String time) {
        return DateTime.parse(time.replace("000000", ""), DateTimeFormat.forPattern("dd.MM.yyyy HH.mm.ss,SSS"));
    }
}
