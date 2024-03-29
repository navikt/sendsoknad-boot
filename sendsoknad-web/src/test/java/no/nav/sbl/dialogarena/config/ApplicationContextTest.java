package no.nav.sbl.dialogarena.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import no.nav.sbl.dialogarena.tokensupport.TokenService;
import no.nav.sbl.soknadinnsending.config.SecurityServiceBeanNames;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.System.setProperty;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SoknadinnsendingConfig.class, MetricsTestConfig.class})
public class ApplicationContextTest {

    private static final String ENVIRONMENT_PROPERTIES = "environment-test.properties";
    private static final String SANITY_URL = "/soknader/api/sanity/skjemautlisting/";
    private static final WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort());

    @MockBean
    DataSource datasource;

    @MockBean(name=SikkerhetsConfig.SOKNAD_FSS_AZUREAD_SERVICE_NAME)
    TokenService azureService;

    @MockBean(name=SikkerhetsConfig.SOKNAD_FSS_TOKENX_SERVICE_NAME)
    TokenService tokenXService;

    @MockBean(name= SecurityServiceBeanNames.SOKNADSMOTTAKER_BEAN_NAME)
    TokenService soknadsmottakerService;

    @MockBean(name= SecurityServiceBeanNames.SOKNADSFILLAGER_BEAN_NAME)
    TokenService soknadsfillagerService;



    @BeforeClass
    public static void beforeClass() {
        loadAndSetProperties();

        initializeSanityMock();
        setProperty("environment.sanitytestport", "" + wireMockServer.port());
        setProperty("environment.istest", "true");

        String value = System.getProperty("user.home") + "dummypath";
        setProperty("sendsoknad.datadir", value);

        setProperty("folder.bilstonad.path", value);
        setProperty("folder.sendsoknad.path", value);
        setProperty("folder.soknad-aap-utland.path", value);
        setProperty("folder.soknadaap.path", value);
        setProperty("folder.refusjondagligreise.path", value);
        setProperty("folder.tilleggsstonader.path", value);
        setProperty("folder.tiltakspenger.path", value);

        setProperty("no.nav.modig.security.sts.url", "dummyvalue");
        setProperty("systemuser.sendsoknad.username", "dummyvalue");
        setProperty("systemuser.sendsoknad.password", "");
        setProperty("kafka.brokers", "");
        setProperty("kafka.applicationId", "");
        setProperty("kafka.topics.messageTopic", "");
        setProperty("kafka.security.enabled", "false");
        setProperty("kafka.security.protocol", "");
        setProperty("kafka.security.trustStorePath", "");
        setProperty("kafka.security.trustStorePassword", "");
        setProperty("kafka.security.keyStorePath", "");
        setProperty("kafka.security.trustStorePassword", "");
        setProperty("kafka.security.keyStorePassword", "");

    }

    @Test
    public void shouldSetupAppContext() {
    }


    @AfterClass
    public static void afterClass() {
        wireMockServer.stop();
    }

    private static void loadAndSetProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = ApplicationContextTest.class.getClassLoader().getResourceAsStream(ENVIRONMENT_PROPERTIES)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            setProperty((String) entry.getKey(), (String) entry.getValue());
        }
    }

    private static void initializeSanityMock() {

        wireMockServer.stubFor(get(urlEqualTo(SANITY_URL))
                .withHeader("Accept", equalTo("application/json, application/*+json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"Skjemaer\":[]}")));
        wireMockServer.start();
    }
}