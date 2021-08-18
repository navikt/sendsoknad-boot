package no.nav.sbl.sendsoknad;

import static org.slf4j.LoggerFactory.getLogger;

import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.web.embedded.JettyWebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;

@Configuration
public class JettyConfig {
	private static final Logger logger = getLogger(JettyConfig.class);

	
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
        	((WebAppContext) server.getHandler()).setSecurityHandler(constraintSecurityHandler);
       // 	((WebAppContext) server.getHandler()).getSecurityHandler().setLoginService(loginService); 
        };// .setSecurityHandler(constraintSecurityHandler);
    }

    @Bean
    ConstraintSecurityHandler constraintSecurityHandler(final LoginService loginService) {
        final ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();

        securityHandler.setLoginService(loginService);
        
       Constraint constraint = new Constraint();
       constraint.setName("Auth");
       constraint.setDataConstraint(Constraint.DC_CONFIDENTIAL);
     //  constraint.setAuthenticate(true);
       ConstraintMapping mapping = new ConstraintMapping();
       mapping.setPathSpec("/*");
       mapping.setConstraint(constraint);
       securityHandler.addConstraintMapping(mapping);
       securityHandler.setLoginService(loginService);
       
       securityHandler.setAuthenticator(new BasicAuthenticator());
        // getConstraintMappings().forEach(securityHandler::addConstraintMapping);

        return securityHandler;
    }
   
    
    @Bean
    LoginService loginService()  {
    	
    	JAASLoginService jaas = new JAASLoginService("OpenAM Realm");
    	jaas.setLoginModuleName("openam");
    	return jaas;
    }
}
