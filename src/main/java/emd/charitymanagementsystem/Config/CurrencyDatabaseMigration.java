package emd.charitymanagementsystem.Config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

/** Backfills historical EUR amounts before Hibernate updates the production schema. */
@Configuration
public class CurrencyDatabaseMigration {
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.driver-class-name", havingValue = "org.postgresql.Driver")
    DataSourceScriptDatabaseInitializer currencyDatabaseInitializer(DataSource dataSource) {
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setMode(DatabaseInitializationMode.ALWAYS);
        settings.setSchemaLocations(List.of("classpath:db/startup/currencies.sql"));
        settings.setSeparator("@@");
        return new DataSourceScriptDatabaseInitializer(dataSource, settings);
    }
}
