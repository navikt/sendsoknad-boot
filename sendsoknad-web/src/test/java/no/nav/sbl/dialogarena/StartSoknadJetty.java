package no.nav.sbl.dialogarena;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.ModigSecurityConstants.SYSTEMUSER_PASSWORD;
import static no.nav.modig.core.context.ModigSecurityConstants.SYSTEMUSER_USERNAME;
import static no.nav.sbl.dialogarena.common.jetty.Jetty.usingWar;
import static no.nav.sbl.dialogarena.config.SystemProperties.setFrom;
import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;
import static no.nav.sbl.dialogarena.test.FilesAndDirs.TEST_RESOURCES;
import static no.nav.sbl.dialogarena.test.FilesAndDirs.WEBAPP_SOURCE;

import java.io.File;
import java.io.IOException;

import javax.sql.DataSource;

import no.nav.modig.testcertificates.TestCertificates;
import no.nav.sbl.dialogarena.common.jetty.Jetty;

public final class StartSoknadJetty {

    private static final int PORT = 8181;
    public final Jetty jetty;

    private StartSoknadJetty(Env env, File overrideWebXmlFile, DataSource dataSource) throws Exception {
        this(env, overrideWebXmlFile, dataSource, PORT);
    }

    public StartSoknadJetty(Env env, File overrideWebXmlFile, DataSource dataSource, int port) throws Exception {
        configureSecurity();
        configureLocalConfig();
        disableBatch();
        setProperty("java.security.auth.login.config", env.getLoginConf());
        TestCertificates.setupKeyAndTrustStore();
        
        jetty = usingWar(WEBAPP_SOURCE)
                .at("/sendsoknad")
                .overrideWebXml(overrideWebXmlFile)
                .sslPort(port + 100)
                .addDatasource(dataSource, "jdbc/SoknadInnsendingDS")
                .port(port)
                .buildJetty();
    }

    private void start() {
        jetty.start();
    }

    private void disableBatch() {
        setProperty("sendsoknad.batch.enabled", "false");
    }

    private void configureLocalConfig() throws IOException {
        setFrom("environment-test.properties");
        setProperty("no.nav.sbl.dialogarena.sendsoknad.sslMock", "true");
       

        setProperty("sendsoknad.datadir", System.getProperty("user.home") + "/Temp/sendsoknad/");
    }

    private void configureSecurity() {
        setProperty("no.nav.modig.security.sts.url", "http://e34jbsl01634.devillo.no:8080/SecurityTokenServiceProvider"); // Microscopium U1
        setProperty(SYSTEMUSER_USERNAME, "srvSendsoknad");
        setProperty(SYSTEMUSER_PASSWORD, "test");
        setProperty("org.apache.cxf.stax.allowInsecureParser", "true");
    }

    public enum Env {
        Intellij("web/src/test/resources/login.conf"),
        Eclipse("src/test/resources/login.conf");
        private final String loginConf;

        Env(String loginConf) {
            this.loginConf = loginConf;
        }

        String getLoginConf() {
            return loginConf;
        }
    }

    // For å logge inn lokalt må du sette cookie i selftesten: document.cookie="nav-esso=12312312345-4; path=/sendsoknad/"

    @SuppressWarnings("unused")
    private static class Intellij {
        public static void main(String[] args) throws Exception {
            setFrom("environment-test.properties");
            new StartSoknadJetty(Env.Intellij, new File(TEST_RESOURCES, "override-web.xml"), buildDataSource()).start();
        }
    }

    @SuppressWarnings("unused")
    private static class Eclipse {
        public static void main(String[] args) throws Exception {
            setFrom("environment-test.properties");
            new StartSoknadJetty(Env.Eclipse, new File(TEST_RESOURCES, "override-web.xml"), buildDataSource()).start();
        }
    }
}
