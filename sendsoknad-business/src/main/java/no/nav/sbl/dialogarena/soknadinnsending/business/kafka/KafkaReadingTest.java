package no.nav.sbl.dialogarena.soknadinnsending.business.kafka;

import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

@Component
@Profile("spring|integration")
public class KafkaReadingTest implements KafkaReading {

    private static final Logger logger = getLogger(KafkaReadingTest.class);

    public void consumeTopic() {
        logger.info("KafkaReadingTest called");
    }
}
