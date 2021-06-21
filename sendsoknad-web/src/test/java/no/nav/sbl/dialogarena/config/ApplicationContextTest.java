package no.nav.sbl.dialogarena.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.System.setProperty;
import static no.nav.modig.core.context.ModigSecurityConstants.SYSTEMUSER_PASSWORD;
import static no.nav.modig.core.context.ModigSecurityConstants.SYSTEMUSER_USERNAME;
import static org.mockito.Mockito.mock;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SoknadinnsendingConfig.class})
public class ApplicationContextTest {

    private static final String ENVIRONMENT_PROPERTIES = "/environment-test.properties";
    private static final String URL = "/soknadsveiviserproxy/skjemautlisting/";
    private static WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort());

    @BeforeClass
    public static void beforeClass() throws NamingException {
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
        setProperty(SYSTEMUSER_USERNAME, "dummyvalue");
        setProperty(SYSTEMUSER_PASSWORD, "");

        SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
        builder.bind("jdbc/SoknadInnsendingDS", mock(DataSource.class));
        builder.activate();
    }

    @Test
    public void shouldSetupAppContext() {}


    @AfterClass
    public static void afterClass() {
        wireMockServer.stop();
    }

    private static void loadAndSetProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = Properties.class.getResourceAsStream(ENVIRONMENT_PROPERTIES)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            setProperty((String) entry.getKey(), (String) entry.getValue());
        }
    }

    private static void initializeSanityMock() {

        wireMockServer.stubFor(get(urlEqualTo(URL))
                .withHeader("Accept", equalTo("application/json, application/*+json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"Skjemaer\":[]}")));
        wireMockServer.start();
    }
}
