package no.nav.sbl.sendsoknad;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbConfig;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.nio.charset.Charset;

@Configuration
@Import(DbConfig.class)
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource datasource) {
        ClassicConfiguration config = new ClassicConfiguration();
        config.setBaselineOnMigrate(true);
        config.setDataSource(datasource);
        config.setEncoding(Charset.forName("ISO-8859-1"));
        config.setBaselineVersionAsString("1.31");
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
