package money_transfers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.SQLDialect;

import javax.sql.DataSource;

public class MoneyTransferModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MoneyTransferApplication.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    private ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }

    @Provides
    @Singleton
    private DataSource provideDataSource() {
        HikariConfig hikariConfig = new HikariConfig("src/main/resources/db.properties");
        return new HikariDataSource(hikariConfig);
    }

    @Provides
    @Singleton
    private SQLDialect provideSQLDialect() {
        return SQLDialect.H2;
    }
}
