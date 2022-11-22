package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Service
//@EnableSchedulerLock(defaultLockAtMostFor = "15m")
public class HenvendelseImporter {

    private static final Logger logger = getLogger(HenvendelseImporter.class);
//    private static final String SCHEDULE_TIME = "0 0 4 * * ?"; // Every day at 04 in the morning
    private static final Boolean SAVE_TO_LOCAL_DB = false;
    private static final String DAGPENGER = "DAG";

    private final SoknadDataFletter soknadDataFletter;
    private final SoknadRepository lokalDb;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final SkjemaOppslagService skjemaOppslagService = new SkjemaOppslagService();

    private final String URI;
    private final String USERNAME;
    private final String PASSWORD;


    @Autowired
    public HenvendelseImporter(
            SoknadDataFletter soknadDataFletter,
            SoknadRepository lokalDb,
            @Value("${henvendelsemigrering.uri}") String uri,
            @Value("${henvendelsemigrering.username}") String username,
            @Value("${henvendelsemigrering.password}") String password
    ) {
        this.soknadDataFletter = soknadDataFletter;
        this.lokalDb = lokalDb;
        this.URI = uri;
        this.USERNAME = username;
        this.PASSWORD = password;
        logger.info("URI: {}, Username: {}", uri, username);
    }

//    @Scheduled(cron = SCHEDULE_TIME)
//    @SchedulerLock(name = "slettGamleSoknader", lockAtLeastFor = "10m")
    @PostConstruct
    public void migrateFromHenvendelse() {
        long startTime = System.currentTimeMillis();
        try {
            List<String> behandlingsIds = getBehandlingsIdsToMigrate();
            behandlingsIds.forEach(this::persistInLocalDb);
        } catch (Exception e) {
            logger.error("Migrating from Henvendelse failed!", e);
        }
        logger.info("Done migrating in {}ms", System.currentTimeMillis() - startTime);
    }

    private List<String> getBehandlingsIdsToMigrate() {
        ResponseEntity<String[]> response = fetchFromHenvendelse();

        if (response.getBody() != null) {
            List<String> behandlingsIds = Arrays.asList(response.getBody());
            logger.info("Found {} Soknader to migrate", behandlingsIds.size());
            return behandlingsIds;
        } else {
            logger.error("Found no Soknader to migrate");
            return Collections.emptyList();
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
        if (lokalDb.hentSoknad(behandlingsId) == null) {
            logger.info("{}: About to fetch and persist Soknad in local database", behandlingsId);

            WebSoknad soknad = soknadDataFletter.hentFraHenvendelse(behandlingsId, true);
            if (soknad.getStatus() != SoknadInnsendingStatus.UNDER_ARBEID) {
                logger.error("{}: Soknad had status {}, not {}. Will not persist.",
                        behandlingsId, soknad.getStatus(), SoknadInnsendingStatus.UNDER_ARBEID);
                return;
            }
            if (DAGPENGER.equals(skjemaOppslagService.getTema(soknad.getskjemaNummer()))) {
                logger.info("{}: Soknad is for tema {}; will not persist in local database", behandlingsId, DAGPENGER);
                return;
            }
            Long soknadId = saveToLocalDatabase(soknad);

            logger.info("{}: Done fetching and persisting Soknad with id {} in local database in {}ms.",
                    behandlingsId, soknadId, System.currentTimeMillis() - startTime);
        } else {
            logger.info("{}: Soknad is already in local database", behandlingsId);
        }
    }

    private Long saveToLocalDatabase(WebSoknad soknad) {
        if (SAVE_TO_LOCAL_DB) {
            return lokalDb.opprettSoknad(soknad);
        } else {
            logger.info("{}: Will not persist to database because feature toggle is turned off.", soknad.getBrukerBehandlingId());
            return null;
        }
    }
}
