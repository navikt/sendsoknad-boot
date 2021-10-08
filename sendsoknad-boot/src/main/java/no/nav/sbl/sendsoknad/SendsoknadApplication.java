package no.nav.sbl.sendsoknad;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import no.nav.sbl.dialogarena.config.SikkerhetsConfig;
import no.nav.sbl.dialogarena.config.SoknadinnsendingConfig;
import no.nav.sbl.dialogarena.config.TokenSupportConfig;
import no.nav.sbl.dialogarena.security.ThreadLocalSubjectHandler;


@SpringBootApplication(exclude = {HttpEncodingAutoConfiguration.class})
@EnableCaching
@EnableWebMvc
@Import(value = {JettyConfig.class,SoknadinnsendingConfig.class,SikkerhetsConfig.class,TokenSupportConfig.class})
public class SendsoknadApplication {
	private static final Logger logger = getLogger(SendsoknadApplication.class);


	public static void main(String[] args) {

		final String loginConfFile = "/app/login.conf";// SendsoknadApplication.class.getClassLoader().getResource("login.conf").getFile();
		logger.info("login.conf file location is " + loginConfFile);
		System.setProperty("java.security.auth.login.config", loginConfFile);
	    System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
	    SpringApplication.run(SendsoknadApplication.class, args);
	}

}
