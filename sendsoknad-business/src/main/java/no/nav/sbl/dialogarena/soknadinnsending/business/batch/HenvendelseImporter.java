package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.modig.common.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.EttersendingService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.FERDIG;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@EnableSchedulerLock(defaultLockAtMostFor = "30m")
public class HenvendelseImporter {

    private static final Logger logger = getLogger(HenvendelseImporter.class);
    private static final String SCHEDULE_TIME = "0 45 14 * * ?"; // At 14:45 every day
    private static final Boolean ER_INNSENDTE_SOKNADER_MED_MANGLENDE_VEDLEGG = true;

    private final SoknadDataFletter soknadDataFletter;
    private final EttersendingService ettersendingService;
    private final SoknadRepository lokalDb;

    private final String FILE;
    private final Map<String, String> AKTORMAPPING = new HashMap<>();


    @Autowired
    public HenvendelseImporter(
            SoknadDataFletter soknadDataFletter,
            EttersendingService ettersendingService,
            SoknadRepository lokalDb,
            @Value("${henvendelsemigrering.file}") String file,
            @Value("${henvendelsemigrering.aktormapping}") String aktormapping
    ) {
        this.soknadDataFletter = soknadDataFletter;
        this.ettersendingService = ettersendingService;
        this.lokalDb = lokalDb;
        this.FILE = file;
        logger.info("Henvendelse migration - file: {}, aktor mappings: {}", file, aktormapping.split(",").length);

        for (String mapping : aktormapping.split(",")) {
            String[] pair = mapping.split(":");
            if (pair.length == 2) {
                AKTORMAPPING.put(pair[0], pair[1]);
            } else {
                logger.error("Unexpected mapping");
            }
        }
    }

    @Scheduled(cron = SCHEDULE_TIME)
    @SchedulerLock(name = "slettGamleSoknader", lockAtLeastFor = "15m")
    public void migrateFromHenvendelse() {
        long startTime = System.currentTimeMillis();
        try {
            MDCOperations.putToMDC(MDCOperations.MDC_CALL_ID, MDCOperations.generateCallId());
            List<String> behandlingsIds = getBehandlingsIdsToMigrate();
            long numberOfSoknaderThatWerePersisted = behandlingsIds.stream()
                    .map(this::persistInLocalDb)
                    .filter(persisted -> persisted)
                    .count();
            logger.info("Migrated {} soknader of {} to local database", numberOfSoknaderThatWerePersisted, behandlingsIds.size());

        } catch (Exception e) {
            logger.error("Migrating from Henvendelse failed!", e);
        }
        logger.info("Done migrating in {}ms", System.currentTimeMillis() - startTime);
    }

    private List<String> getBehandlingsIdsToMigrate() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream is = classloader.getResourceAsStream(FILE);
             InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            List<String> ids = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] idAndTimestamp = line.split("\t");
                if (idAndTimestamp.length >= 2 && !"".equals(idAndTimestamp[0])) {
                    ids.add(idAndTimestamp[0]);
                }
            }
            logger.info("Found {} Soknader to migrate from file", ids.size());
            return ids;

        } catch (Exception e) {
            logger.error("Unable to read local file with migration ids");
            return Collections.emptyList();
        }
    }


    private boolean persistInLocalDb(String behandlingsId) {
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
                    soknad = ettersendingService.migrering(behandlingsId, aktor);
                } else {
                    // Will fetch and save to local database:
                    soknad = soknadDataFletter.hentFraHenvendelse(behandlingsId, true);
                }

                persisted = true;
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
}
