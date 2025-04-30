# Trade Ingestion System Requirements

## Project Overview
This project builds a Trade Ingestion System that ingests, validates, transforms, and stores trade deal messages. It uses Solace JMS for messaging, PostgreSQL for data persistence, and Redis for caching. The system is designed to be resilient, scalable, secure, and production-like even in testing. Message order and idempotency are critical.

## Technical Stack
- **Framework**: Java 21, Spring Boot 3.2.3
- **Messaging**: Solace JMS 10.19.0 (async message processing)
- **Database**: PostgreSQL 15 (production and TestContainers for testing)
- **Caching**: Redis 7 (production and TestContainers for testing)
- **Testing**: TestContainers 1.19.3
- **Resilience**: Resilience4j 2.1.0
- **Monitoring**: Spring Boot Actuator 3.2.3

## Core Services
1. **Deal Ingestion Service**
   - JMS message listener for trade deals
   - Message validation and transformation
   - Deal ID generation and tracking
   - Idempotent processing

2. **Deal Validation Service**
   - Business rule validation
   - Schema validation
   - Cross-reference validation

3. **Deal Transformation Service**
   - Data format conversion
   - Field mapping and enrichment
   - Error handling and logging

4. **Deal Storage Service**
   - PostgreSQL persistence
   - Redis caching
   - Query optimization

## Testing Strategy
1. **Unit Tests**
   - Focus on business logic
   - Mock external dependencies (DB, messaging, caching)
   - High test coverage (>80%)
   - Fast execution

2. **Integration Tests**
   - Use TestContainers for PostgreSQL, Redis, and Solace
   - Test full end-to-end flows
   - Environment parity with production
   - Proper cleanup and isolation

3. **Resilience Testing**
   - Circuit breaker testing
   - Retry mechanism validation
   - Rate limiting verification
   - Error handling scenarios

## Resilience Features
1. **Circuit Breaker**
   - Configurable thresholds
   - Graceful degradation
   - Automatic recovery

2. **Retry Mechanism**
   - Configurable retry attempts
   - Exponential backoff
   - Idempotency handling

3. **Rate Limiting**
   - Request throttling
   - Burst handling
   - Queue management

## Security
1. **Authentication**
   - Solace JMS authentication
   - Database credentials
   - Redis access control
   - External authentication service integration
   - JWT token validation
   - Service-to-service authentication

2. **Authorization**
   - Role-based access control (RBAC)
   - Fine-grained permission management
   - Context-based authorization decisions
   - External authorization service integration
   - Operation-level access control
   - Resource-level access control
   - Classification level validation
   - Jurisdiction-based restrictions
   - Custom attribute validation
   - Authorization decision caching
   - Circuit breaker protection
   - Retry mechanism
   - Metrics and monitoring
   - Error handling and fallback

3. **Data Protection**
   - Encryption at rest
   - Secure communication
   - Audit logging
   - PII data masking
   - Data classification
   - Retention policies

4. **Authorization Service Integration**
   - REST API integration
   - Circuit breaker protection
   - Retry mechanism
   - Caching of authorization decisions
   - Metrics and monitoring
   - Error handling and fallback
   - Context-based authorization checks
   - Classification level validation
   - Jurisdiction-based restrictions
   - Role-based access control
   - Custom attribute validation

## Critical Behaviors
1. **Message Ordering**
   - Strict sequence: Create → Modify → Cancel → Settlement
   - Order preservation
   - Out-of-order detection

2. **Idempotency**
   - Duplicate detection
   - Retry handling
   - State management

3. **Error Handling**
   - Graceful degradation
   - Error recovery
   - Dead letter queues

## Monitoring
1. **Health Checks**
   - Spring Boot Actuator
   - Custom health indicators
   - Dependency monitoring

2. **Metrics**
   - Performance metrics
   - Error rates
   - Latency tracking

3. **Logging**
   - Structured logging
   - Log aggregation
   - Audit trails

## Configuration
1. **Externalized Configs**
   - Database settings
   - Messaging configs
   - Cache settings

2. **Profiles**
   - Development (dev)
   - Testing (test)
   - Production (prod)

3. **Environment Variables**
   - Service endpoints
   - Credentials
   - Feature flags

## Deployment
1. **Containerization**
   - Docker images
   - Container orchestration
   - Service discovery

2. **CI/CD**
   - Maven-based builds
   - Automated testing
   - Deployment pipelines

3. **Infrastructure**
   - Kubernetes deployment
   - Load balancing
   - Scaling policies

## Documentation
1. **API Documentation**
   - REST endpoints
   - Message schemas
   - Error codes

2. **Configuration Guide**
   - Environment setup
   - Service configuration
   - Security settings

3. **Testing Guide**
   - Test environment setup
   - Running tests
   - Test coverage

## Current Progress
✅ Completed:
- Basic project structure
- Solace JMS integration
- PostgreSQL setup
- Redis caching
- TestContainers configuration
- Basic health checks
- Profile-based configuration
- Initial unit tests
- Basic integration tests
- Resilience4j setup
- Docker configuration
- CI/CD pipeline setup
- Authorization service implementation
  - RBAC system
  - Context-based authorization
  - Operation-level access control
  - Resource-level access control
  - Authorization decision caching
  - Classification level validation
  - Jurisdiction-based restrictions
  - Custom attribute validation

🔄 In Progress:
- Advanced validation rules
- Complex transformations
- Performance optimization
- Security hardening
- Monitoring enhancements
- Documentation completion

⏳ Pending:
- Advanced error handling
- Production deployment
- Load testing
- Security audit
- Performance tuning
- Final documentation

## Notes
- Unit tests mock Solace/Postgres/Redis
- Integration tests use real TestContainers
- No H2 to avoid environment drift
- System aims for production parity in tests
- Focus on maintainability and scalability
- Emphasis on testing and monitoring

## Message Sequencing Requirements

### Core Requirements
1. **Event Ordering**
   - All deal events must be processed in the correct sequence
   - Events must follow the deal lifecycle: CREATE -> MODIFY -> SETTLEMENT/CANCEL
   - No events should be processed after CANCEL or SETTLEMENT
   - Version numbers must be strictly increasing

2. **Distributed Processing**
   - Support multiple instances for high availability and scalability
   - Ensure consistent ordering across all instances
   - Prevent race conditions in distributed environment
   - Handle network partitions and instance failures

3. **Message Validation**
   - Validate event sequence based on deal lifecycle
   - Detect and handle duplicate messages
   - Prevent processing of outdated messages
   - Maintain version consistency

4. **Error Handling**
   - Stash out-of-order messages for later processing
   - Automatic retry of stashed messages
   - Configurable retry intervals and backoff
   - Dead letter queue for failed messages

5. **Monitoring and Metrics**
   - Track sequence violations
   - Monitor lock acquisition failures
   - Measure processing latency
   - Track message stashing and retry statistics

### Technical Requirements
1. **Distributed Locking**
   - Use Redis for distributed locks
   - Implement lock timeouts and automatic release
   - Handle lock acquisition failures gracefully
   - Support lock retry with backoff

2. **State Management**
   - Maintain deal state in Redis
   - Track last processed version
   - Store event sequence history
   - Implement TTL for cleanup

3. **Performance**
   - Minimize lock contention
   - Optimize Redis operations
   - Support high message throughput
   - Handle message bursts

4. **Reliability**
   - Ensure message persistence
   - Handle Redis failures
   - Implement circuit breakers
   - Support graceful degradation

## Additional Requirements
1. **Monitoring**
   - Track sequence violations
   - Monitor lock acquisition
   - Measure processing latency
   - Track message stashing

2. **Alerting**
   - Alert on sequence violations
   - Notify on lock acquisition failures
   - Monitor retry queue size
   - Track error rates

3. **Operational**
   - Support manual intervention
   - Provide admin tools
   - Enable configuration changes
   - Support maintenance windows 