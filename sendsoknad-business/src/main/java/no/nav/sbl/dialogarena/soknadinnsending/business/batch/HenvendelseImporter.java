package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.modig.common.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Service
@EnableSchedulerLock(defaultLockAtMostFor = "30m")
public class HenvendelseImporter {

    private static final Logger logger = getLogger(HenvendelseImporter.class);
    private static final String SCHEDULE_TIME = "0 00 20 * * ?"; // At 20:00 every day

    private final SoknadDataFletter soknadDataFletter;
    private final SoknadRepository lokalDb;
    private final RestTemplate restTemplate = new RestTemplate();

    private final String FILE;
    private final String URI;
    private final String USERNAME;
    private final String PASSWORD;


    @Autowired
    public HenvendelseImporter(
            SoknadDataFletter soknadDataFletter,
            SoknadRepository lokalDb,
            @Value("${henvendelsemigrering.file}") String file,
            @Value("${henvendelsemigrering.uri}") String uri,
            @Value("${henvendelsemigrering.username}") String username,
            @Value("${henvendelsemigrering.password}") String password
    ) {
        this.soknadDataFletter = soknadDataFletter;
        this.lokalDb = lokalDb;
        this.FILE = file;
        this.URI = uri;
        this.USERNAME = username;
        this.PASSWORD = password;
        logger.info("FILE: {}, URI: {}, Username: {}", file, uri, username);
    }

    @Scheduled(cron = SCHEDULE_TIME)
    @SchedulerLock(name = "slettGamleSoknader", lockAtLeastFor = "15m")
    public void migrateFromHenvendelse() {
        long startTime = System.currentTimeMillis();
        try {
            MDCOperations.putToMDC(MDCOperations.MDC_CALL_ID, MDCOperations.generateCallId());
            List<String> behandlingsIds = getBehandlingsIdsToMigrate();
            behandlingsIds.forEach(this::persistInLocalDb);
        } catch (Exception e) {
            logger.error("Migrating from Henvendelse failed!", e);
        }
        logger.info("Done migrating in {}ms", System.currentTimeMillis() - startTime);
    }

    private List<String> getBehandlingsIdsToMigrate() {
        try {
            return getBehandlingsIdsToMigrateFromHenvendelse();
        } catch (Exception e) {
            return getBehandlingsIdsToMigrateFromFile();
        }
    }

    private List<String> getBehandlingsIdsToMigrateFromFile() {
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

    private List<String> getBehandlingsIdsToMigrateFromHenvendelse() {
        ResponseEntity<String[]> response = fetchFromHenvendelse();

        if (response.getBody() != null) {
            List<String> behandlingsIds = Arrays.asList(response.getBody());
            logger.info("Found {} Soknader to migrate from Henvendelse", behandlingsIds.size());
            return behandlingsIds;
        } else {
            logger.error("Found no Soknader to migrate from Henvendelse");
            throw new RuntimeException("Found no Soknader to migrate from Henvendelse");
        }
    }

    private ResponseEntity<String[]> fetchFromHenvendelse() {
        return restTemplate.exchange(URI, HttpMethod.GET, new HttpEntity<>(basicAuthHeaders()), String[].class);
    }

    private HttpHeaders basicAuthHeaders() {
        return new HttpHeaders() {{
            String auth = USERNAME + ":" + PASSWORD;
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};
    }

    private void persistInLocalDb(String behandlingsId) {
        long startTime = System.currentTimeMillis();
        try {
            if (lokalDb.hentSoknad(behandlingsId) == null) {
                logger.info("{}: About to fetch and persist Soknad in local database", behandlingsId);

                // Will fetch and save to local database:
                WebSoknad soknad = soknadDataFletter.hentFraHenvendelse(behandlingsId, true);

                if (soknad.getStatus() == SoknadInnsendingStatus.UNDER_ARBEID) {
                    logger.info("{}: Done fetching and persisting Soknad in local database in {}ms.",
                            behandlingsId, System.currentTimeMillis() - startTime);
                } else {
                    logger.error("{}: Soknad had status {}, not {} - did not persist. Time taken: {}ms.",
                            behandlingsId, soknad.getStatus(), SoknadInnsendingStatus.UNDER_ARBEID,
                            System.currentTimeMillis() - startTime);
                }

            } else {
                logger.info("{}: Soknad is already in local database", behandlingsId);
            }
        } catch (Exception e) {
            logger.error("{}: Failed to fetch and persist Soknad. Time taken: {}",
                    behandlingsId, System.currentTimeMillis() - startTime , e);
        }
    }
}
