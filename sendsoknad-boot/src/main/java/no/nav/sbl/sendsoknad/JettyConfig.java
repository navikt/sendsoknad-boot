package no.nav.sbl.sendsoknad;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;

import org.apache.commons.collections15.map.HashedMap;
import org.eclipse.jetty.security.LoginService;
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

@Configuration
@EnableConfigurationProperties
public class JettyConfig {
    private static final Logger logger = getLogger(JettyConfig.class);

    @Autowired
    Environment env;

    @Bean(name = "kravdialoginformasjon")
    @ConfigurationProperties(prefix = "kravdialoginformasjon")
    public Map<String, String> kravinformasjonMap() {
        return new HashedMap<String, String>();
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
    JettyServerCustomizer jettyServerCustomizer() {
        return server -> {

           
            SessionCookieConfig cookieConfig = ((WebAppContext) server.getHandler()).getSessionHandler()
                    .getSessionCookieConfig();
            cookieConfig.setName("SENDSOKNAD_BOOT_JSESSIONID");
            cookieConfig.setHttpOnly(true);
            cookieConfig.setMaxAge(30);

            Map<String, String> initParams = ((WebAppContext) server.getHandler()).getInitParams();
            initParams.put("useFileMappedBuffer", "false");
            initParams.put("org.eclipse.jetty.servlet.SessionIdPathParameterName", "none");

            ((WebAppContext) server.getHandler()).getSessionHandler()
                    .setSessionTrackingModes(java.util.Set.of(SessionTrackingMode.COOKIE));

        };
    }

}
