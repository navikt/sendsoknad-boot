package no.nav.sbl.dialogarena.soknadinnsending.business;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@EnableScheduling
public class ServicesApplicationConfig {

    private static final Logger logger = getLogger(ServicesApplicationConfig.class);

    private final File brukerprofilDataDirectory;


    @Autowired
    public ServicesApplicationConfig(
            @Value("${sendsoknad.datadir}") File brukerprofilDataDirectory
    ) {
        super();
        this.brukerprofilDataDirectory = brukerprofilDataDirectory;
    }

}
