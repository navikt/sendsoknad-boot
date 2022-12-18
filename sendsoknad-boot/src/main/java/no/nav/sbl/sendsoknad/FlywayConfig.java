package no.nav.sbl.sendsoknad;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbConfig;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

@Configuration
@Import(DbConfig.class)
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource datasource) {
        Flyway flyway = new Flyway();
        flyway.setBaselineOnMigrate(true);
        flyway.setDataSource(datasource);
        flyway.setBaselineOnMigrate(true);
        flyway.setEncoding("ISO-8859-1");
        flyway.setBaselineVersionAsString("1.31");
        flyway.setTable("schema_version");
        flyway.setValidateOnMigrate(false);
        flyway.setOutOfOrder(false);
        return flyway;
    }

    @Bean
    public FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway, null);
    }
}
