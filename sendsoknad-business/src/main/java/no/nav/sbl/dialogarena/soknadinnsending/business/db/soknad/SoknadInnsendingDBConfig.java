package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
	
	
//	environment.istest
	
	@Bean
	@ConditionalOnProperty(prefix = "environment" ,name = "istest", havingValue = "true")
	public DataSource dataSource() {
        JndiDataSourceLookup lookup = new JndiDataSourceLookup();
        return lookup.getDataSource("jdbc/SoknadInnsendingDS");
    }

    /*@Bean
    public DataSource dataSource() {
        JndiDataSourceLookup lookup = new JndiDataSourceLookup();
        return lookup.getDataSource("jdbc/SoknadInnsendingDS");
    }
    
	
	@Bean
    public DataSource dataSource() throws SQLException {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setURL(url);
        dataSource.setImplicitCachingEnabled(true);
        dataSource.setFastConnectionFailoverEnabled(true);
        return dataSource;
    }
    
    */

    @Bean
    public PlatformTransactionManager transactionManager(DataSource datasource) {
        return new DataSourceTransactionManager(datasource);
    }

    @Bean
    public Pingable dbPing(DataSource datasource) {
        return new Pingable() {
            @Override
            public Ping ping() {
                PingMetadata metadata = new PingMetadata("jdbc/SoknadInnsendingDS", "JDBC:Sendsøknad Database", true);
                try {
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
                    jdbcTemplate.queryForList("select * from dual");
                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }
}
