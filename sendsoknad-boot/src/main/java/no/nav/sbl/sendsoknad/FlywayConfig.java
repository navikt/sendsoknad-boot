package no.nav.sbl.sendsoknad;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbConfig;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.util.Arrays;

@Configuration
@Import(DbConfig.class)
public class FlywayConfig {

    private final Environment env;

    public FlywayConfig(Environment env) {
        this.env = env;
    }


    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource datasource) {
        ClassicConfiguration config = new ClassicConfiguration();
        config.setBaselineOnMigrate(true);
        config.setDataSource(datasource);
        config.setEncoding(Charset.forName("ISO-8859-1"));

        // Når det kjøres lokalt vil vi starte på migrasjon v1 og ikke v1.31
        if (!Arrays.asList(env.getActiveProfiles()).contains("local")) {
            config.setBaselineVersionAsString("1.31");
        }

        config.setTable("schema_version");
        config.setValidateOnMigrate(false);
        config.setOutOfOrder(false);
        Flyway flyway = new Flyway(config);
        return flyway;
    }

    @Bean
    public FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway, null);
    }
}
