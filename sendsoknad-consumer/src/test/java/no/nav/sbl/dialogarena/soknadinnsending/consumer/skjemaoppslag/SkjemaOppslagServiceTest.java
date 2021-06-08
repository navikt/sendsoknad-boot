package no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;
import static org.junit.Assert.*;

@RunWith(value = MockitoJUnitRunner.class)
public class SkjemaOppslagServiceTest {

    private static final String SKJEMANUMMER = "NAV 11-13.05";
    private static final String ILLEGAL_SKJEMANUMMER = "illegal skjemanummer";
    private static final String SKJEMANUMMER_THAT_GETS_ADDED = "NAV 71-71.71";

    private static final String URL = "/soknadsveiviserproxy/skjemautlisting/";
    private static WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort());

    private static String jsonResponse;
    private SkjemaOppslagService skjemaOppslagService;


    @BeforeClass
    public static void beforeClass() {
        wireMockServer.start();
    }

    @AfterClass
    public static void afterClass() {
        wireMockServer.stop();
    }

    @Before
    public void setup() throws IOException {
        System.setProperty("environment.sanitytestport", "" + wireMockServer.port());
        System.setProperty("environment.istest", "true");
        jsonResponse = readJsonResponseDataFromDisk();
        skjemaOppslagService = new SkjemaOppslagService();
    }

    @Test
    public void getTittel_vedleggsId_returnsTittel() throws IOException {
        initializeCache();

        String result = skjemaOppslagService.getTittel("X5");

        assertEquals("Originalkvittering", result);
    }

    @Test
    public void getUrl_vedleggsId_throwsException() throws IOException {
        initializeCache();

        String result = skjemaOppslagService.getUrl("X5");

        assertEquals("", result);
    }

    @Test
    public void getTittel_properSkjemanummer_returnsTittel() throws IOException {
        initializeCache();

        String result = skjemaOppslagService.getTittel(SKJEMANUMMER);

        assertEquals("Søknad om arbeidsavklaringspenger", result);
    }

    @Test
    public void getTittel_illegalSkjemanummer_throwsException() throws IOException {
        initializeCache();
        try {
            skjemaOppslagService.getTittel(ILLEGAL_SKJEMANUMMER);
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertEquals("Failed to find tittel for 'illegal skjemanummer'", e.getMessage());
        }
    }

    @Test
    public void getTema_properSkjemanummer_returnsTema() throws IOException {
        initializeCache();

        String result = skjemaOppslagService.getTema(SKJEMANUMMER);

        assertEquals("AAP", result);
    }

    @Test
    public void getTema_illegalSkjemanummer_throwsException() throws IOException {
        initializeCache();
        try {
            skjemaOppslagService.getTema(ILLEGAL_SKJEMANUMMER);
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertEquals("Failed to find tema for 'illegal skjemanummer'", e.getMessage());
        }
    }

    @Test
    public void getUrl_properSkjemanummer_returnsUrl() throws IOException {
        initializeCache();

        String result = skjemaOppslagService.getUrl(SKJEMANUMMER);

        assertTrue(result.contains("https://cdn.sanity.io/"));
    }

    @Test
    public void getUrl_illegalSkjemanummer_throwsException() throws IOException {
        initializeCache();

        String result = skjemaOppslagService.getUrl(ILLEGAL_SKJEMANUMMER);

        assertEquals("", result);
    }


    @Test
    public void getTittel_skjemanummerWithVedleggsId_returnsTittel() throws IOException {
        initializeCache();

        String result = skjemaOppslagService.getTittel("NAV 10-07.73");

        assertEquals("T12 Tilleggsskjema for hjelpemidler og tilrettelegging i arbeidslivet", result);
    }

    @Test
    public void getTittel_vedleggsIdInsteadOfSkjemanummer_returnsTittel() throws IOException {
        initializeCache();

        String result = skjemaOppslagService.getTittel("A4");

        assertEquals("T12 Tilleggsskjema for hjelpemidler og tilrettelegging i arbeidslivet", result);
    }

    @Test
    public void getTittel_failToUpdateCache_returnsOldCachedTittel() throws IOException {
        initializeCache();
        failToUpdateCache();

        String result = skjemaOppslagService.getTittel("A4");

        assertEquals("T12 Tilleggsskjema for hjelpemidler og tilrettelegging i arbeidslivet", result);
    }

    @Test
    public void getTittel_cacheIsUpdated() throws IOException {
        initializeCache();
        try {
            skjemaOppslagService.getTittel(SKJEMANUMMER_THAT_GETS_ADDED);
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertEquals("Failed to find tittel for '" + SKJEMANUMMER_THAT_GETS_ADDED + "'", e.getMessage());
        }

        updateCacheWithNewSkjemanummer();

        String result = skjemaOppslagService.getTittel(SKJEMANUMMER_THAT_GETS_ADDED);

        assertEquals("Made up skjemanummer", result);
    }


    @Test
    public void getL7Data_fromHardcodedList_returnsData() throws IOException {
        String skjemanummer = SKJEMANUMMER_KVITTERING;
        initializeCache();

        String tittel = skjemaOppslagService.getTittel(skjemanummer);
        String tema = skjemaOppslagService.getTema(skjemanummer);
        String url = skjemaOppslagService.getUrl(skjemanummer);

        assertEquals("Kvitteringsside for dokumentinnsending", tittel);
        assertEquals("GEN", tema);
        assertEquals("", url);
    }

    @Test
    public void get11_12_10Data_fromHardcodedList_returnsData() throws IOException {
        String skjemanummer = "NAV 11-12.10";
        initializeCache();

        String tittel = skjemaOppslagService.getTittel(skjemanummer);
        String tema = skjemaOppslagService.getTema(skjemanummer);
        String url = skjemaOppslagService.getUrl(skjemanummer);

        assertEquals("Kjøreliste for godkjent bruk av egen bil", tittel);
        assertEquals("TSO", tema);
        assertTrue(url.contains("https://cdn.sanity.io/"));
    }

    @Test
    public void get11_12_11Data_fromHardcodedList_returnsData() throws IOException {
        String skjemanummer = "NAV 11-12.11";
        initializeCache();

        String tittel = skjemaOppslagService.getTittel(skjemanummer);
        String tema = skjemaOppslagService.getTema(skjemanummer);
        String url = skjemaOppslagService.getUrl(skjemanummer);

        assertEquals(" Kjøreliste for godkjent bruk av egen bil arbeidssøker", tittel);
        assertEquals("TSR", tema);
        assertTrue(url.contains("https://cdn.sanity.io/"));
    }

    @Test
    public void get11_12_12Data_fromHardcodedList_returnsData() throws IOException {
        String skjemanummer = "NAV 11-12.12";
        initializeCache();

        String tittel = skjemaOppslagService.getTittel(skjemanummer);
        String tema = skjemaOppslagService.getTema(skjemanummer);
        String url = skjemaOppslagService.getUrl(skjemanummer);

        assertEquals("Søknad om tilleggsstønader", tittel);
        assertEquals("TSO", tema);
        assertTrue(url.contains("https://cdn.sanity.io/"));
    }

    @Test
    public void get11_12_13Data_fromHardcodedList_returnsData() throws IOException {
        String skjemanummer = "NAV 11-12.13";
        initializeCache();

        String tittel = skjemaOppslagService.getTittel(skjemanummer);
        String tema = skjemaOppslagService.getTema(skjemanummer);
        String url = skjemaOppslagService.getUrl(skjemanummer);

        assertEquals("Søknad om tilleggsstønader arbeidssøker", tittel);
        assertEquals("TSR", tema);
        assertTrue(url.contains("https://cdn.sanity.io/"));
    }


    private static String readJsonResponseDataFromDisk() throws IOException {
        try (InputStream is = SkjemaOppslagService.class.getClassLoader().getResourceAsStream("sanity.json")) {
            assert is != null;
            String json = IOUtils.toString(is, UTF_8);
            assert !"".equals(json);
            return json;
        }
    }

    private void initializeCache() throws IOException {
        wireMockServer.stubFor(get(urlEqualTo(URL))
                .withHeader("Accept", equalTo("application/json, application/*+json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse)));

        skjemaOppslagService.init();
    }

    private void failToUpdateCache() {
        wireMockServer.stubFor(get(urlEqualTo(URL))
                .withHeader("Accept", equalTo("application/json, application/*+json"))
                .willReturn(aResponse()
                        .withStatus(500)));

        skjemaOppslagService.refreshCache();
    }

    private void updateCacheWithNewSkjemanummer() {
        String response = jsonResponse.substring(0, jsonResponse.length() - 2);
        response += ",{\"Skjemanummer\":\"" + SKJEMANUMMER_THAT_GETS_ADDED + "\"," +
                "\"Vedleggsid\":\"\"," +
                "\"Tittel\":\"Made up skjemanummer\"," +
                "\"Tema\":\"SYK\"," +
                "\"Gosysid\":0" +
                "}]}";

        wireMockServer.stubFor(get(urlEqualTo(URL))
                .withHeader("Accept", equalTo("application/json, application/*+json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(response)));

        skjemaOppslagService.refreshCache();
    }
}
