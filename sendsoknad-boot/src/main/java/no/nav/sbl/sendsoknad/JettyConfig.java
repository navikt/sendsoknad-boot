package no.nav.sbl.sendsoknad;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import javax.activation.FileDataSource;

import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.web.embedded.JettyWebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sun.security.auth.login.ConfigFile;

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
        	
        	final String override = SendsoknadApplication.class.getClassLoader().getResource("override-web.xml").getFile();
    	//	logger.info("override-web.xml file location is " + override);
        	logger.info("Setting loginService");
        //	((WebAppContext) server.getHandler()).addOverrideDescriptor(override);
        	((WebAppContext) server.getHandler()).setSecurityHandler(constraintSecurityHandler);
       // 	((WebAppContext) server.getHandler()).getSecurityHandler().setLoginService(loginService); 
        };// .setSecurityHandler(constraintSecurityHandler);
    }

    @Bean
    ConstraintSecurityHandler constraintSecurityHandler(final LoginService loginService) {
        final ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        //loginService.setIdentityService(new DefaultIdentityService());
        securityHandler.setLoginService(loginService);
        
       Constraint constraint = new Constraint();
       constraint.setName("OpenAM Realm");
     //  constraint.setAuthenticate(false);
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
    	final String loginConfFile = SendsoknadApplication.class.getClassLoader().getResource("login.conf").getFile();
    	logger.info("login.conf file location is " + loginConfFile);
    	File file = (new File(loginConfFile));
    	logger.info("uri is " + file.toURI());
    	ConfigFile configfile = new ConfigFile(file.toURI());
    	jaas.setConfiguration(configfile);
    	return jaas;
    }
}
