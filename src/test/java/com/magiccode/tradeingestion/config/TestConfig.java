package com.magiccode.tradeingestion.config;

import jakarta.jms.ConnectionFactory;
import jakarta.persistence.EntityManagerFactory;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.GenericContainer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.util.Properties;

import com.magiccode.tradeingestion.service.DealValidationService;
import com.magiccode.tradeingestion.service.DealTransformationService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.magiccode.tradeingestion.util.DatabaseTestUtils;

@TestConfiguration
@EnableTransactionManagement
@Configuration
@Import({SolaceContainerConfig.class, RedisTestConfig.class})
public class TestConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("testdb")
                .addScript("classpath:db/migration/V1__Initial_schema.sql")
                .addScript("classpath:db/migration/V2__Add_external_system_refs.sql")
                .addScript("classpath:db/migration/V3__Add_additional_indexes_and_constraints.sql")
                .addScript("classpath:db/migration/V4__Update_schema_for_JSON_alignment.sql")
                .addScript("classpath:db/migration/V5__Update_booking_info_schema.sql")
                .addScript("classpath:db/migration/V7__Update_fixed_income_derivative_deal_id_to_uuid.sql")
                .addScript("classpath:db/migration/V8__Fix_deal_leg_and_notional_amount.sql")
                .build();
    }

    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        return flyway;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.magiccode.tradeingestion.model");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        em.setJpaProperties(properties);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }

    @Bean
    @Primary
    public ConnectionFactory jmsConnectionFactory(GenericContainer<?> solaceContainer) {
        try {
            SolConnectionFactory connectionFactory = SolJmsUtility.createConnectionFactory();
            connectionFactory.setHost(solaceContainer.getHost());
            connectionFactory.setPort(solaceContainer.getMappedPort(55556));
            connectionFactory.setVPN("default");
            connectionFactory.setUsername("admin");
            connectionFactory.setPassword("admin");
            connectionFactory.setDirectTransport(false);
            connectionFactory.setReconnectRetries(3);
            return (ConnectionFactory) connectionFactory;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Solace JMS connection factory", e);
        }
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setConcurrency("3-10");
        factory.setSessionTransacted(true);
        factory.setAutoStartup(false);
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setSessionTransacted(true);
        return template;
    }

    @Bean
    public DealValidationService dealValidationService() {
        return Mockito.mock(DealValidationService.class);
    }

    @Bean
    public DealTransformationService dealTransformationService() {
        return Mockito.mock(DealTransformationService.class);
    }

    @Bean
    public DatabaseTestUtils databaseTestUtils(JdbcTemplate jdbcTemplate, Flyway flyway) {
        return new DatabaseTestUtils(jdbcTemplate, flyway);
    }
} 