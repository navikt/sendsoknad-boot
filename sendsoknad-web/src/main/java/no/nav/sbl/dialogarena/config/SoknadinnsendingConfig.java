package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.soknadinnsending.business.BusinessConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadInnsendingDBConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ConsumerConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@EnableAspectJAutoProxy
@Configuration
@Import({
        ApplicationConfig.class,
        BusinessConfig.class,
        CacheConfig.class,
        ConsumerConfig.class,
        SoknadInnsendingDBConfig.class,
        HandlebarsHelperConfig.class,
        MetricsConfig.class
})
@ComponentScan(basePackages = "no.nav.sbl.dialogarena.rest")
public class SoknadinnsendingConfig {
}
