package info.jtrac;

import java.io.File;
import javax.sql.DataSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for tests that can test either the service layer or dao or both.
 * Configured with SpringExtension and @Transactional for JUnit 5.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
    "file:src/main/webapp/WEB-INF/applicationContext.xml",
    "file:src/main/webapp/WEB-INF/applicationContext-lucene.xml",
    "file:src/main/webapp/WEB-INF/applicationContext-scheduler.xml"
})
@Transactional
public abstract class JtracTestBase {

    protected static class Assert extends org.junit.jupiter.api.Assertions {}

    static {
        File home = new File("target/home");
        if (!home.exists()) {
            home.mkdirs();
        }
        System.setProperty("jtrac.home", home.getAbsolutePath());
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected Jtrac jtrac;

    @Autowired
    protected JtracDao dao;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setDao(JtracDao dao) {
        this.dao = dao;
    }

    public void setJtrac(Jtrac jtrac) {
        this.jtrac = jtrac;
    }

    protected void deleteFromTables(String... tableNames) {
        if (jdbcTemplate != null && tableNames != null) {
            for (String table : tableNames) {
                jdbcTemplate.execute("delete from " + table);
            }
        }
    }

    protected void setComplete() {
        TestTransaction.flagForCommit();
    }

    protected void endTransaction() {
        TestTransaction.end();
    }

    protected void startNewTransaction() {
        TestTransaction.start();
    }
}
