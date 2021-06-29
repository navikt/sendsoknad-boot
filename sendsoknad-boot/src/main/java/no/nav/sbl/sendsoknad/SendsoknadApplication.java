package no.nav.sbl.sendsoknad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import no.nav.sbl.dialogarena.config.SikkerhetsConfig;
import no.nav.sbl.dialogarena.config.SoknadinnsendingConfig;


@SpringBootApplication
@Import(value = {SoknadinnsendingConfig.class,SikkerhetsConfig.class})
public class SendsoknadApplication {

	public static void main(String[] args) {
		SpringApplication.run(SendsoknadApplication.class, args);
	}

}
