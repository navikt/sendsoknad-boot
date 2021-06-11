package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

@Configuration
@EnableTransactionManagement
public class SoknadInnsendingDBConfig {

    @Bean
    public DataSource dataSource() {
        JndiDataSourceLookup lookup = new JndiDataSourceLookup();
        return lookup.getDataSource("jdbc/SoknadInnsendingDS");
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public Pingable dbPing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                PingMetadata metadata = new PingMetadata("jdbc/SoknadInnsendingDS", "JDBC:Sendsøknad Database", true);
                try {
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource());
                    jdbcTemplate.queryForList("select * from dual");
                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }
}
