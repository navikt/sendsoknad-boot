package no.nav.sbl.sendsoknad;

import no.nav.sbl.dialogarena.config.SikkerhetsConfig;
import no.nav.sbl.dialogarena.config.SoknadinnsendingConfig;
import no.nav.sbl.dialogarena.config.TokenSupportConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication(exclude = {HttpEncodingAutoConfiguration.class})
@EnableCaching
@EnableWebMvc
@Import(value = {JettyConfig.class, SoknadinnsendingConfig.class, SikkerhetsConfig.class, TokenSupportConfig.class, FlywayConfig.class})
public class SendsoknadApplication {
    public static void main(String[] args) {
        SpringApplication.run(SendsoknadApplication.class, args);
    }
}
