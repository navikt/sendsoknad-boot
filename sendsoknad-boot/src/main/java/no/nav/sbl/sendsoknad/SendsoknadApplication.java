package no.nav.sbl.sendsoknad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import no.nav.sbl.dialogarena.config.SikkerhetsConfig;
import no.nav.sbl.dialogarena.config.SoknadinnsendingConfig;
import no.nav.sbl.dialogarena.security.ThreadLocalSubjectHandler;


@SpringBootApplication(exclude = {HttpEncodingAutoConfiguration.class})
@EnableCaching
@EnableWebMvc
@Import(value = {SoknadinnsendingConfig.class,SikkerhetsConfig.class})
public class SendsoknadApplication {

	public static void main(String[] args) {
	    System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
		SpringApplication.run(SendsoknadApplication.class, args);
	}

}
