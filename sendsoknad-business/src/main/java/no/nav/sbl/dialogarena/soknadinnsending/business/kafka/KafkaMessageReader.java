package no.nav.sbl.dialogarena.soknadinnsending.business.kafka;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadArkiveringsStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class KafkaMessageReader implements CommandLineRunner {

    @Value("${kafka.brokers}")
    private String bootstrapServers;
    @Value("${kafka.applicationId}")
    private String groupId;
    @Value("${kafka.topics.messageTopic}")
    private String topic;
    @Value("${kafka.security.enabled}")
    private Boolean security;
    @Value("${kafka.security.protocol}")
    private String protocol;
    @Value("${kafka.security.trustStorePath}")
    private String trustStorePath;
    @Value("${kafka.security.trustStorePassword}")
    private String trustStorePassword;
    @Value("${kafka.security.keyStorePath}")
    private String keyStorePath;
    @Value("${kafka.security.keyStorePassword}")
    private String keyStorePassword;

    private final SoknadRepository soknadRepository;
    private static final Logger logger = getLogger(KafkaMessageReader.class);


    @Autowired
    public KafkaMessageReader(
            @Qualifier("soknadInnsendingRepository") SoknadRepository soknadRepository) {
        this.soknadRepository = soknadRepository;
    }

    private void consumeTopic() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 5000);
        if (security) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, protocol);
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStorePath);
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, trustStorePassword);
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStorePath);
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keyStorePassword);
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keyStorePassword);
        }
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        logger.info("***Start polling av message topic");

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(5000));
            for (ConsumerRecord<String, String> record : records) {
                String key = record.key();
                if (record.value().startsWith("**Archiving: OK")) {
                    if (soknadRepository.updateArkiveringsStatus(key, SoknadArkiveringsStatus.Arkivert) > 0) {
                        logger.info(key + ": er arkivert");
                    }
                } else if (record.value().startsWith("**Archiving: FAILED")) {
                    if (soknadRepository.updateArkiveringsStatus(key, SoknadArkiveringsStatus.ArkiveringFeilet) > 0) {
                        logger.error(key + ": arkivering feilet");
                    }
                }
            }
            consumer.commitSync();
            logger.debug("**Ferdig behandlet mottatte meldinger.");
        }
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("**Start running kafkaConsumer");
        consumeTopic();
    }

}