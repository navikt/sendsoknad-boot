package no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

@EnableScheduling
@Service
public class SkjemaOppslagService {
    private static final Logger logger = getLogger(SkjemaOppslagService.class);

    public static final String SKJEMANUMMER_KVITTERING = "L7";

    private static final String SKJEMAUTLISTING_URL = "https://tjenester.nav.no/soknadsveiviserproxy/skjemautlisting";
    private static final String SKJEMAUTLISTING_TEST_URL = "http://localhost:{port}/soknadsveiviserproxy/skjemautlisting/";
    private static final long UPDATE_INTERVAL_IN_MS = 10 * 60 * 1000;
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final List<SkjemaOgVedleggsdata> hardcodedLocalList = Collections.singletonList(
        skjemaOgVedleggsdata(SKJEMANUMMER_KVITTERING, "Kvitteringsside for dokumentinnsending", "GEN") // L7 er ikke noe man kan søke på,
            // eller noe som brukeren skal sende inn, men en tweak som har blitt lagt til for å få med den pdfen i våres systemer. L7 skal ikke være i Sanity.
    );

    private static String url;
    private static List<SkjemaOgVedleggsdata> sanityList = new ArrayList<>();


    public String getTittel(String skjemanummer) {
        return getProperty(skjemanummer, "tittel", SkjemaOgVedleggsdata::getTittel);
    }

    public String getTema(String skjemanummer) {
        return getProperty(skjemanummer, "tema", SkjemaOgVedleggsdata::getTema);
    }

    public String getUrl(String skjemanummer) {
        try {
            return getProperty(skjemanummer, "url", SkjemaOgVedleggsdata::getUrl);
        } catch (Exception e) {
            logger.debug("Unable to find URL for skjemanummer " + skjemanummer);
            return "";
        }
    }

    private static String getProperty(String id, String property, Function<SkjemaOgVedleggsdata, String> propertyGetter) {
        return sanityList.stream()
                .filter(data -> id.equals(data.getSkjemanummer()) || id.equals(data.getVedleggsid()))
                .map(propertyGetter)
                .filter(Objects::nonNull)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Failed to find " + property + " for '" + id + "'"));
    }


    @PostConstruct
    void init() throws IOException {
        try {
            boolean isBeingRunAsTest = isBeingRunAsTest();
            url = isBeingRunAsTest ? SKJEMAUTLISTING_TEST_URL.replace("{port}", getTestPort()) : SKJEMAUTLISTING_URL;
            logger.info("Is being run as test: {}", isBeingRunAsTest);

            initializeFromOldResult();
            sanityList.addAll(addHardcodedListToSanityData(sanityList));
        } catch (Exception e) {
            logger.error("Unable to query Sanity for data. The application wont be able to operate without initial data on startup. Error: ", e);
            throw e;
        }
    }

    @Scheduled(fixedRate = UPDATE_INTERVAL_IN_MS)
    void refreshCache() {
        logger.info("Refreshing Sanity cache");
        try {
            List<SkjemaOgVedleggsdata> list = refreshSanityData();
            list.addAll(addHardcodedListToSanityData(list));
            sanityList = list;
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
        }
    }

    private List<SkjemaOgVedleggsdata> addHardcodedListToSanityData(List<SkjemaOgVedleggsdata> sanityList) {
        List<SkjemaOgVedleggsdata> hardcodedList = new ArrayList<>();

        for (SkjemaOgVedleggsdata data : hardcodedLocalList) {
            if (sanityDoesNotContainData(sanityList, data)) {
                hardcodedList.add(data);
            }
        }
        return hardcodedList;
    }

    private static boolean sanityDoesNotContainData(List<SkjemaOgVedleggsdata> sanityList, SkjemaOgVedleggsdata data) {
        List<String> sanitySkjemanummer = sanityList.stream().map(SkjemaOgVedleggsdata::getSkjemanummer).collect(Collectors.toList());
        List<String> sanityVedleggsIds  = sanityList.stream().map(SkjemaOgVedleggsdata::getVedleggsid)  .collect(Collectors.toList());
        return !sanitySkjemanummer.contains(data.getSkjemanummer()) && !sanityVedleggsIds.contains(data.getSkjemanummer());
    }

    private static List<SkjemaOgVedleggsdata> refreshSanityData() {
        try {
            return REST_TEMPLATE.getForObject(url, Skjemaer.class).getSkjemaer();

        } catch (Exception e) {
            throw new RuntimeException("Unable to query " + url, e);
        }
    }

    private static boolean isBeingRunAsTest() {

        for (String envVar : System.getProperties().stringPropertyNames()) {
            if (envVar.startsWith("environment.istest")) {
                return Boolean.parseBoolean(System.getProperty("environment.istest"));
            }
        }
        return false;
    }

    private static String getTestPort() {

        for (String envVar : System.getProperties().stringPropertyNames()) {
            if (envVar.startsWith("environment.sanitytestport")) {
                return System.getProperty(envVar);
            }
        }
        return "";
    }

    @SuppressWarnings("SameParameterValue")
    private static SkjemaOgVedleggsdata skjemaOgVedleggsdata(String skjemanummer, String tittel, String tema) {
        SkjemaOgVedleggsdata data = new SkjemaOgVedleggsdata();
        data.setSkjemanummer(skjemanummer);
        data.setTittel(tittel);
        data.setTema(tema);
        return data;
    }

    private static void initializeFromOldResult() throws IOException {
        String oldSanityResponse = readJsonResponseDataFromDisk();
        ObjectMapper jsonMapper = new ObjectMapper();
        sanityList = jsonMapper.readValue(oldSanityResponse, Skjemaer.class).getSkjemaer();
    }

    private static String readJsonResponseDataFromDisk() throws IOException {
        try (InputStream is = SkjemaOppslagService.class.getClassLoader().getResourceAsStream("sanity.json")) {
            assert is != null;
            String json = IOUtils.toString(is, UTF_8);
            assert !"".equals(json);
            return json;
        }
    }
}
