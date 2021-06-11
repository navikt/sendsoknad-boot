package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import javax.sql.DataSource;


public class TestSupport extends JdbcDaoSupport implements RepositoryTestSupport {

    @Autowired
    public TestSupport(DataSource dataSource) {
        super.setDataSource(dataSource);
    }
}
