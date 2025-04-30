# Deal Ingestion Service

A Spring Boot application for deal data ingestion with resilience patterns, caching, and monitoring.

## Features

- Solace JMS integration for message processing
- Redis caching for improved performance
- PostgreSQL persistence
- Comprehensive validation
- Resilience patterns (Circuit Breaker, Rate Limiter, Retry, Time Limiter)
- Metrics and monitoring with Micrometer
- Async processing support
- External authentication/authorization service integration
- Role-based access control
- Fine-grained permission management
- Authorization decision caching
- Context-based authorization with:
  - Classification level checks
  - Jurisdiction-based restrictions
  - Role-based access control
  - Custom attribute validation

## Prerequisites

- Java 21
- Maven 3.8+
- PostgreSQL 14+
- Redis 6+
- Solace PubSub+ broker
- Authentication/Authorization service

## Configuration

The application uses Spring Boot's configuration system. Create an `application.yml` file based on the `application-example.yml` template:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/deals
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
  cache:
    type: redis
    redis:
      time-to-live: 3600000

solace:
  jms:
    host: your_solace_host
    username: your_username
    password: your_password
    vpn: your_vpn

auth:
  service:
    url: ${AUTH_SERVICE_URL}
    timeout: 5000
    retry:
      max-attempts: 3
      backoff: 1000
    circuit-breaker:
      failure-rate-threshold: 50
      wait-duration: 5000
    cache:
      ttl: 300000  # 5 minutes
    context:
      classification-levels:
        - PUBLIC
        - CONFIDENTIAL
        - SECRET
      jurisdictions:
        - AU
        - US
        - UK
      roles:
        - trader
        - risk-analyst
        - compliance
```

## Testing

The project uses a structured approach to testing with clear separation between unit and integration tests.

### Test Structure
- Unit tests extend `LightweightUnitTest` to avoid loading the full Spring context
- Unit tests are located in `src/test/java/com/magiccode/tradeingestion/unit/`
- Integration tests are organized by component:
  - Solace integration tests: `src/test/java/com/magiccode/tradeingestion/integration/solace/`
  - Redis integration tests: `src/test/java/com/magiccode/tradeingestion/integration/cache/`
  - PostgreSQL integration tests: `src/test/java/com/magiccode/tradeingestion/integration/database/`
- Base test configurations are in `src/test/java/com/magiccode/tradeingestion/config/`

### Running Tests

#### Unit Tests Only
```bash
mvn test
```
This runs all unit tests that extend `LightweightUnitTest` or are located in the `unit/` directory.

#### Specific Unit Test
```bash
mvn test -Dtest=YourUnitTestClass
```

#### Integration Tests
The project provides several integration test profiles for different components:

##### All Integration Tests
```bash
mvn verify -P integration-tests
```

##### Solace Integration Tests
```bash
mvn verify -P solace-integration-tests
```

##### Redis Integration Tests
```bash
mvn verify -P redis-integration-tests
```

##### PostgreSQL Integration Tests
```bash
mvn verify -P postgres-integration-tests
```

#### All Tests
```bash
mvn clean verify
```

### Test Configuration
- Unit tests extend `LightweightUnitTest` for minimal Spring context and test data support
- Integration tests extend appropriate base classes (e.g., `BasePostgresIntegrationTest`, `BaseRedisIntegrationTest`)
- Database tests use `PostgresTestConfig` for PostgreSQL container
- Cache tests use `RedisTestConfig` for Redis container
- Solace tests use `SolaceContainerConfig` for Solace container

### Test Data Management
- Test data is managed through `TestDataFactory` and `TestDataConfig`
- JSON test files are located in `src/test/resources/testdata/`
- Test data is cached for performance in CI/CD environments
- Double-caching strategy (JSON content and deserialized objects) for optimal test performance

### Test Profiles
The project uses Maven profiles to manage different test types:
- `unit-tests`: Runs only unit tests (default)
- `integration-tests`: Runs all integration tests
- `solace-integration-tests`: Runs only Solace integration tests
- `redis-integration-tests`: Runs only Redis integration tests
- `postgres-integration-tests`: Runs only PostgreSQL integration tests

Each integration test profile:
- Uses TestContainers for containerized testing
- Excludes unit tests
- Enables verbose TestContainers output
- Disables container reuse for test isolation

## Building and Running

### Local Development

#### Prerequisites
1. Java 21
2. PostgreSQL 15
3. Docker (for Solace)
4. Redis

#### Database Setup
1. Install PostgreSQL 15
2. Create the database:
   ```sql
   CREATE DATABASE trade_ingestion;
   ```
3. Create a user (if not already exists):
   ```sql
   CREATE USER postgres WITH PASSWORD 'postgres';
   GRANT ALL PRIVILEGES ON DATABASE trade_ingestion TO postgres;
   ```

#### Solace Setup
1. Start Solace container:
   ```bash
   docker run -d -p 55555:55555 -p 8008:8008 -p 1883:1883 -p 8000:8000 -p 5672:5672 -p 9000:9000 -p 2222:2222 \
   --shm-size=2g --env username_admin_globalaccesslevel=admin --env username_admin_password=admin \
   --name=solace solace/solace-pubsub-standard
   ```

#### Redis Setup
1. Install Redis
2. Start Redis server:
   ```bash
   redis-server
   ```

#### Running the Application
1. Build the application:
   ```bash
   mvn clean install
   ```

2. Run with local profile:
   ```