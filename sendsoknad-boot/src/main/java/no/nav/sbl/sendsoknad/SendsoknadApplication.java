package no.nav.sbl.sendsoknad;

import static org.slf4j.LoggerFactory.getLogger;

import no.nav.modig.common.SpringContextAccessor;
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


@SpringBootApplication(exclude = {HttpEncodingAutoConfiguration.class})
@EnableCaching
@EnableWebMvc
@Import(value = {SpringContextAccessor.class,JettyConfig.class,SoknadinnsendingConfig.class,SikkerhetsConfig.class,TokenSupportConfig.class,FlywayConfig.class})
public class SendsoknadApplication {
	private static final Logger logger = getLogger(SendsoknadApplication.class);


	public static void main(String[] args) {
	    SpringApplication.run(SendsoknadApplication.class, args);
	}

}
