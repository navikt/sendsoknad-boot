package no.nav.sbl.sendsoknad;

import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.boot.autoconfigure.web.embedded.JettyWebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JettyConfig {

	
	@Bean
	WebServerFactoryCustomizer embeddedServletContainerCustomizer(final JettyServerCustomizer jettyServerCustomizer) {
	
        return container -> {
            if (container instanceof JettyServletWebServerFactory) {
                ((JettyServletWebServerFactory) container).addServerCustomizers(jettyServerCustomizer);
            }
        };
    }

    @Bean
    JettyServerCustomizer jettyServerCustomizer(final LoginService loginService) {
        return server -> ((WebAppContext) server.getHandler()).getSecurityHandler().setLoginService(loginService);// .setSecurityHandler(constraintSecurityHandler);
    }
/*
    @Bean
    ConstraintSecurityHandler constraintSecurityHandler(final LoginService loginService) {
        final ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();

        securityHandler.setLoginService(loginService);
       // getConstraintMappings().forEach(securityHandler::addConstraintMapping);

        return securityHandler;
    }
    */
    
    @Bean
    LoginService loginService()  {
    	
    	JAASLoginService jaas = new JAASLoginService("OpenAM Realm");
    	jaas.setLoginModuleName("openam");
    	return jaas;
    }
}
