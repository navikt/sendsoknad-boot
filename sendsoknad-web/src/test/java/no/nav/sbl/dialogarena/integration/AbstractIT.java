package no.nav.sbl.dialogarena.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import no.nav.sbl.dialogarena.StartSoknadJetty;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.test.TestProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;
import static no.nav.sbl.dialogarena.test.FilesAndDirs.TEST_RESOURCES;

public abstract class AbstractIT {
    private static final int PORT = 10001;
    private static final String URL = "/soknadsveiviserproxy/skjemautlisting/";


    private static StartSoknadJetty jetty;
    private static WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort());

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty(TestProperties.CONTAINER_FACTORY, "org.glassfish.jersey.test.external.ExternalTestContainerFactory");
        System.setProperty(TestProperties.CONTAINER_PORT, "" + PORT);
        System.setProperty(TestProperties.LOG_TRAFFIC, "true");
        System.setProperty("jersey.test.host", "localhost");
        System.setProperty("no.nav.sbl.dialogarena.sendsoknad.hsqldb", "true");

        initializeSanityMock();
        System.setProperty("environment.sanitytestport", "" + wireMockServer.port());
        System.setProperty("environment.istest", "true");

        jetty = new StartSoknadJetty(
                StartSoknadJetty.Env.Intellij,
                new File(TEST_RESOURCES, "override-web-integration.xml"),
                buildDataSource("hsqldb.properties"),
                PORT
        );
        jetty.jetty.start();
    }

    @AfterClass
    public static void afterClass() {
        if (jetty != null) {
            jetty.jetty.stop.run();
        }
        wireMockServer.stop();
    }

    protected SoknadTester soknadMedDelstegstatusOpprettet(String skjemanummer) {
        try {
            return SoknadTester.startSoknad(skjemanummer)
                    .settDelstegstatus("opprettet")
                    .hentSoknad()
                    .hentFakta();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Kunne ikke opprette søknad");
        }
    }

    private static void initializeSanityMock() throws IOException {

        wireMockServer.stubFor(get(urlEqualTo(URL))
                .withHeader("Accept", equalTo("application/json, application/*+json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readJsonResponseDataFromDisk())));
        wireMockServer.start();
    }

    private static String readJsonResponseDataFromDisk() throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream is = classloader.getResourceAsStream("sanity.json")) {
            assert is != null;
            return IOUtils.toString(is, UTF_8);
        }
    }
}
