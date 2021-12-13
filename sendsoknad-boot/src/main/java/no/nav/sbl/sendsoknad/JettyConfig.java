package no.nav.sbl.sendsoknad;

import com.sun.security.auth.login.ConfigFile;
import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@EnableConfigurationProperties
public class JettyConfig {
    private static final Logger logger = getLogger(JettyConfig.class);

    @Autowired
    Environment env;

    @Bean(name = "kravdialoginformasjon")
    @ConfigurationProperties(prefix = "kravdialoginformasjon")
    public Map<String, String> kravinformasjonMap() {
        return new HashMap<>();
    }


    @Bean
    WebServerFactoryCustomizer embeddedServletContainerCustomizer(final JettyServerCustomizer jettyServerCustomizer) {

        return container -> {
            if (container instanceof JettyServletWebServerFactory) {
                logger.info("Adding jetty server customizer");
                ((JettyServletWebServerFactory) container).addServerCustomizers(jettyServerCustomizer);
            }
        };
    }

    @Bean
    JettyServerCustomizer jettyServerCustomizer(final LoginService loginService,ConstraintSecurityHandler constraintSecurityHandler) {
        return server -> {

            logger.info("Setting loginService");
            SessionCookieConfig cookieConfig = ((WebAppContext) server.getHandler()).getSessionHandler().getSessionCookieConfig();
            cookieConfig.setName("SENDSOKNAD_BOOT_JSESSIONID");
            cookieConfig.setHttpOnly(true);
            cookieConfig.setMaxAge(30);
            logger.info("Setting session cookie");

            Map<String, String> initParams = ((WebAppContext) server.getHandler()).getInitParams();
            initParams.put("useFileMappedBuffer", "false");
            initParams.put("org.eclipse.jetty.servlet.SessionIdPathParameterName", "none");

            ((WebAppContext) server.getHandler()).getSessionHandler().setSessionTrackingModes(java.util.Set.of(SessionTrackingMode.COOKIE));
            ((WebAppContext) server.getHandler()).setSecurityHandler(constraintSecurityHandler);
        };
    }

    @Bean
    ConstraintSecurityHandler constraintSecurityHandler(final LoginService loginService) {
        final ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setLoginService(loginService);

       Constraint constraint = new Constraint();
       constraint.setName("OpenAM Realm");
       constraint.setRoles(new String[] {Constraint.ANY_AUTH});
       ConstraintMapping mapping = new ConstraintMapping();
       mapping.setPathSpec("/*");
       mapping.setConstraint(constraint);
       securityHandler.addConstraintMapping(mapping);
       securityHandler.setLoginService(loginService);

       securityHandler.setAuthenticator(new BasicAuthenticator());
       securityHandler.setIdentityService(new DefaultIdentityService());
        return securityHandler;
    }


    @Bean
    LoginService loginService()  {
        JAASLoginService jaas = new JAASLoginService("OpenAM Realm");
        jaas.setLoginModuleName("openam");
        final String loginConfFile;
        if (Arrays.asList(env.getActiveProfiles()).contains("local")) {
            loginConfFile = "./target/classes/login.conf";
        } else {
            loginConfFile = "/app/login.conf";
        }
        logger.info("login.conf file location is " + loginConfFile);
        File file = (new File(loginConfFile));
        logger.info("uri is " + file.toURI());
        ConfigFile configfile = new ConfigFile(file.toURI());
        jaas.setConfiguration(configfile);
        return jaas;
    }
}
