package no.nav.sbl.dialogarena.soknadinnsending.business.kafka;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadArkiveringsStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class KafkaMessageReader implements CommandLineRunner {

    private final KafkaReading kafkaReading;

    @Autowired
    public KafkaMessageReader(KafkaReading kafkaReading) {
        this.kafkaReading = kafkaReading;
    }


    @Override
    public void run(String... args) {
        kafkaReading.consumeTopic();
    }

}