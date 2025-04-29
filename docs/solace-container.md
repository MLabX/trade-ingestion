# Solace Container Setup and Configuration

## Container Information

The Solace container is running with the following specifications:

- **Container ID**: 6f435c42365e
- **Container Name**: great_satoshi
- **Image**: solace/solace-pubsub-standard:latest
- **Status**: Running
- **Port Mappings**:
  - MQTT: 1883 → 62861
  - AMQP: 5672 → 62863
  - REST: 8008 → 62864
  - SEMP: 8080 → 62865
  - WebSocket: 9000 → 62860
  - SMF: 55555 → 62862

## Connection Information

To connect to the Solace container, use the following localhost ports:

- **MQTT**: `localhost:62861`
- **AMQP**: `localhost:62863`
- **REST**: `localhost:62864`
- **SEMP**: `localhost:62865`
- **WebSocket**: `localhost:62860`
- **SMF**: `localhost:62862`

## Container Status Check

To check the container status, use:
```bash
docker ps | grep solace
```

To view container logs, use:
```bash
docker logs <container_name_or_id>
```

## Configuration Settings

### Working Configuration
```yaml
environment:
  - username_admin_globalaccesslevel=admin
  - username_admin_password=admin
  - system_scaling_maxconnectioncount=100
  - system_scaling_maxqueuesize=500
  - service_semp_plaintext_port=8080
  - service_smf_port=55555
  - service_smf_compressed_port=55003
  - service_web_transport_port=8008
  - service_mqtt_tcp_port=1883
  - service_amqp_port=5672
```

### Important Notes
1. Do not set `system_scaling_maxqueuemessagecount` - the container interprets any value as megabytes (M) and will fail to start
2. Use dynamic port mappings instead of fixed ports
3. Use SMF port 55555 instead of 55556
4. Let Docker generate container names instead of using fixed names

## Common Warnings

The following warnings are normal in containerized environments and don't affect functionality:

1. System resource checks:
   - Unable to read BIOS information
   - CPU feature checks
   - These are expected in containerized environments

2. Startup sequence:
   - Platform type determination
   - License file generation
   - Pre-startup checks

## Troubleshooting

If you encounter issues:

1. Check container status:
   ```bash
   docker ps | grep solace
   ```

2. View logs:
   ```bash
   docker logs <container_name_or_id>
   ```

3. Verify port mappings:
   ```bash
   docker port <container_name_or_id>
   ```

## Failed Configurations to Avoid

1. Setting `system_scaling_maxqueuemessagecount`:
   - Value 100000M: "Solace PubSub+ Standard Edition does not support 100000M queue messages"
   - Value 10000M: "Solace PubSub+ Standard Edition does not support 10000M queue messages"
   - Value 1000M: "Solace PubSub+ Standard Edition does not support 1000M queue messages"
   - Solution: Remove this setting entirely and use default values

2. Fixed port mappings:
   - Can cause conflicts with other services
   - Solution: Use dynamic port mappings

3. Fixed container names:
   - Can cause conflicts when restarting containers
   - Solution: Let Docker generate unique names

## Best Practices

1. Always verify container status before attempting connections
2. Monitor logs for any unusual errors
3. Use appropriate port mappings for your protocol
4. Keep the container image updated to the latest version
5. Use dynamic port mappings instead of fixed ports
6. Let Docker manage container names
7. Avoid setting `system_scaling_maxqueuemessagecount`
8. Use SMF port 55555 instead of 55556

## Test Environment Setup

For test environments, we use fixed ports and dedicated configurations to ensure consistency and reliability:

1. **Test-Specific Configuration Files**:
   - `docker-compose.test.yml`: Fixed port mappings for test environment
   - `scripts/start-solace-test.sh`: Test-specific start script
   - `scripts/setup-solace-test.sh`: Test-specific setup script
   - `src/test/java/com/magiccode/tradeingestion/config/SolaceContainerConfig.java`: Test container configuration

2. **Test Environment Ports**:
   - MQTT: 1883
   - AMQP: 5672
   - REST: 8008
   - SEMP: 8080
   - WebSocket: 9000
   - SMF: 55555

3. **Test Environment Configuration**:
   - VPN: "default"
   - Queues:
     - DEAL.IN: Exclusive queue for incoming deals
     - DEAL.OUT: Exclusive queue for outgoing deals
     - DEAL.DLQ: Exclusive queue for dead letter messages
   - Subscriptions:
     - DEAL.IN → DEAL.IN
     - DEAL.OUT → DEAL.OUT
     - DEAL.DLQ → DEAL.DLQ
   - Client Profile: "default"
     - Allows guaranteed message send/receive
     - Allows guaranteed endpoint creation
     - Allows transacted sessions
   - Test Client:
     - Username: "test-client"
     - Password: "test-password"
     - Profile: "default"

4. **Test Environment Best Practices**:
   - Use fixed ports for test environments
   - Use dedicated container names (e.g., "solace-test")
   - Use separate volume names for test data
   - Run tests in isolation from development environment
   - Clean up test containers after test completion
   - Ensure all required queues and subscriptions are created
   - Configure appropriate client profiles and permissions

5. **Running Tests**:
   ```bash
   # Start test container
   ./scripts/start-solace-test.sh
   
   # Run tests
   ./mvnw test
   
   # Clean up
   docker stop solace-test && docker rm solace-test
   ```

6. **Test Container Management**:
   - Test containers should be started before test execution
   - Test containers should be cleaned up after test completion
   - Test containers should use fixed ports for consistent test execution
   - Test containers should be isolated from development containers
   - Test containers should have all required queues and subscriptions configured
   - Test containers should have appropriate client profiles and permissions 