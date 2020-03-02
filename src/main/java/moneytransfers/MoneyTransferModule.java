package moneytransfers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import org.apache.commons.dbcp.BasicDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MoneyTransferModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoneyTransferModule.class);

    @Override
    protected void configure() {
        loadDbProperties();
    }

    private void loadDbProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("src/main/resources/db.properties")) {
            properties.load(input);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        Names.bindProperties(binder(), properties);
    }

    @Provides
    @Singleton
    private ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }

    @Provides
    @Singleton
    private DSLContext provideDslContext(@Named("url") String url,
                                         @Named("username") String username,
                                         @Named("password") String password,
                                         @Named("autoCommit") boolean autoCommit,
                                         @Named("sqlDialect") String sqlDialect) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDefaultAutoCommit(autoCommit);
        return DSL.using(dataSource, SQLDialect.valueOf(sqlDialect),
                new Settings().withExecuteWithOptimisticLocking(true));
    }
}
