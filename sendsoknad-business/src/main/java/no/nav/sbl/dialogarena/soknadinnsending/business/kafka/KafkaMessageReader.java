package no.nav.sbl.dialogarena.soknadinnsending.business.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
public class  KafkaMessageReader implements CommandLineRunner {

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