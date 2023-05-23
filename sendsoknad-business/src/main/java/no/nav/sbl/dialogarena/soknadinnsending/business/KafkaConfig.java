package no.nav.sbl.dialogarena.soknadinnsending.business;

import no.nav.sbl.dialogarena.soknadinnsending.business.kafka.KafkaMessageReader;
import no.nav.sbl.dialogarena.soknadinnsending.business.kafka.KafkaReadingImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        KafkaMessageReader.class,
        KafkaReadingImpl.class
})
public class KafkaConfig {
}