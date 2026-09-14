package emd.charitymanagementsystem;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.*;

class ImpactMigrationTests {
    @Test void existingEventsStayPrivateAfterMigration() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:event-migration;MODE=PostgreSQL", "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("create table event (id bigint primary key)");
            statement.execute("insert into event (id) values (1)");
            var migration = new ClassPathResource("db/manual/2026-09-14-public-events.sql");
            ScriptUtils.executeSqlScript(connection, migration);
            ScriptUtils.executeSqlScript(connection, migration);
            statement.execute("insert into event (id) values (2)");
            try (var rows = statement.executeQuery("select public_visible from event")) {
                for (int i = 0; i < 2; i++) {
                    assertTrue(rows.next()); assertFalse(rows.getBoolean(1)); assertFalse(rows.wasNull());
                }
            }
        }
    }
    @Test void existingAndNewRowsDefaultToPrivateAndMigrationIsRepeatable() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:impact-migration;MODE=PostgreSQL", "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("create table project (id bigint primary key, name varchar(255))");
            statement.execute("insert into project (id, name) values (1, 'Existing private project')");
            var migration = new ClassPathResource("db/manual/2026-09-14-public-impact.sql");
            ScriptUtils.executeSqlScript(connection, migration);
            ScriptUtils.executeSqlScript(connection, migration);
            statement.execute("insert into project (id, name) values (2, 'New private project')");
            try (var rows = statement.executeQuery("select public_impact from project order by id")) {
                for (int i = 0; i < 2; i++) {
                    assertTrue(rows.next()); assertFalse(rows.getBoolean(1)); assertFalse(rows.wasNull());
                }
                assertFalse(rows.next());
            }
        }
    }
}
