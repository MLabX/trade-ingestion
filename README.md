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

## Prerequisites

- Java 21
- Maven 3.8+
- PostgreSQL 14+
- Redis 6+
- Solace PubSub+ broker

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
```

## Building and Running

### Local Development

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/deal-ingestion-1.0.0.jar
```

### Docker

```bash
# Build the Docker image
docker build -t deal-ingestion-service .

# Run the container
docker run -p 8080:8080 \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e SOLACE_HOST=your_solace_host \
  -e SOLACE_USERNAME=your_username \
  -e SOLACE_PASSWORD=your_password \
  deal-ingestion-service
```

### Kubernetes

```bash
# Apply the Kubernetes manifests
kubectl apply -f k8s/
```

## API Endpoints

- `POST /api/deals` - Create a new deal
- `GET /api/deals/{id}` - Get a deal by ID
- `GET /api/deals` - Get all deals
- `GET /api/deals/symbol/{symbol}` - Get deals by symbol

## Resilience Patterns

The application implements several resilience patterns:

- **Circuit Breaker**: Prevents cascading failures by stopping requests to failing services
- **Rate Limiter**: Controls the rate of requests to prevent overload
- **Retry**: Automatically retries failed operations
- **Time Limiter**: Prevents long-running operations from blocking the system

## Monitoring

The application exposes several monitoring endpoints:

- `/actuator/health` - Application health status
- `/actuator/metrics` - Application metrics
- `/actuator/circuitbreakers` - Circuit breaker status
- `/actuator/ratelimiters` - Rate limiter status
- `/actuator/retries` - Retry status

## Testing

```bash
# Run tests
mvn test

# Run tests with coverage
mvn verify
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
