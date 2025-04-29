package com.magiccode.tradeingestion.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.wait.strategy.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.Properties;

/**
 * Configuration for integration tests that uses test containers.
 * 
 * This configuration provides:
 * 1. PostgreSQL container for database testing
 * 2. Solace container for message broker testing
 * 3. Redis configuration for caching
 * 4. JPA and transaction management setup
 * 
 * The configuration uses test containers to provide isolated
 * environments for integration testing.
 * 
 * @see PostgreSQLContainer
 * @see GenericContainer
 * @see RedisConfig
 * @see SolaceConfig
 */
@TestConfiguration
@ActiveProfiles("integration-test")
@Import({
    RedisConfig.class,
    SolaceConfig.class
})
public class IntegrationTestConfig {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationTestConfig.class);
    private static final String POSTGRES_IMAGE = "postgres:15-alpine";
    private static final String SOLACE_IMAGE = "solace/solace-pubsub-standard:latest";
    private static final Duration CONTAINER_STARTUP_TIMEOUT = Duration.ofMinutes(2);
    private static final Duration SOLACE_STARTUP_TIMEOUT = Duration.ofMinutes(5);

    /**
     * Creates and configures a PostgreSQL container for testing.
     *
     * @return A configured PostgreSQL container
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withStartupTimeout(CONTAINER_STARTUP_TIMEOUT);
    }

    /**
     * Creates and configures a Solace container for testing.
     *
     * @return A configured Solace container
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public GenericContainer<?> solaceContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(SOLACE_IMAGE))
            .withExposedPorts(55555)
            .withEnv("username_admin_globalaccesslevel", "admin")
            .withEnv("username_admin_password", "admin")
            .withEnv("system_scaling_maxconnectioncount", "100")
            .withEnv("system_scaling_maxqueuemessagecount", "1000000")
            .withEnv("system_scaling_maxqueuesize", "1000")
            .withCreateContainerCmdModifier(cmd -> {
                cmd.withMemory(4L * 1024 * 1024 * 1024); // 4GB memory
                cmd.withCpuShares(2); // 2 CPU shares
            })
            .waitingFor(Wait.forLogMessage(".*Solace PubSub\\+ Broker is running.*", 1)
                .withStartupTimeout(SOLACE_STARTUP_TIMEOUT));

        try {
            container.start();
            logger.info("Solace container started successfully on port: {}", 
                container.getMappedPort(55555));
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Stopping Solace container...");
                container.stop();
                logger.info("Solace container stopped");
            }));
        } catch (Exception e) {
            logger.error("Failed to start Solace container", e);
            throw new IllegalStateException("Failed to start Solace container", e);
        }

        return container;
    }

    /**
     * Creates a data source using the PostgreSQL container configuration.
     *
     * @return A configured Hikari data source
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        final PostgreSQLContainer<?> postgres = postgresContainer();
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setDriverClassName(postgres.getDriverClassName());
        return new HikariDataSource(config);
    }

    /**
     * Creates an entity manager factory for JPA operations.
     *
     * @param dataSource The data source to use
     * @return A configured entity manager factory
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            final DataSource dataSource) {
        final LocalContainerEntityManagerFactoryBean em = 
            new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.magiccode.tradeingestion.model");

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        em.setJpaVendorAdapter(vendorAdapter);

        final Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", 
            "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.hbm2ddl.auto", "none");
        em.setJpaProperties(properties);

        return em;
    }

    /**
     * Creates a transaction manager for JPA operations.
     *
     * @param entityManagerFactory The entity manager factory to use
     * @return A configured transaction manager
     */
    @Bean
    public PlatformTransactionManager transactionManager(
            final LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
} 