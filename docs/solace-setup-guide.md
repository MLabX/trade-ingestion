# Solace PubSub+ Setup Guide for Testing

## Overview
This guide provides step-by-step instructions for setting up a local Solace PubSub+ instance for testing the Trade Ingestion Service. The setup includes creating necessary queues and subscriptions for message processing.

## Prerequisites
- Docker installed on your machine
- At least 4GB of memory available
- At least 2 CPU cores available
- Ports 8080, 55554, 8008, 5672, 1883, and 9000 available

## Step 1: Start Solace Container
Run the following command to start the Solace container:

```bash
docker run -d --name=solace \
  -p 8080:8080 \
  -p 55554:55555 \
  -p 8008:8008 \
  -p 5672:5672 \
  -p 1883:1883 \
  -p 9000:9000 \
  --shm-size=1g \
  --memory="4g" \
  --cpus="2.0" \
  -e username_admin_globalaccesslevel=admin \
  -e username_admin_password=admin \
  solace/solace-pubsub-standard
```

## Step 2: Verify Container Status
Check if the container is running properly:

```bash
docker ps | grep solace
```

You should see the solace container in the running state.

## Step 3: Wait for Solace to Initialize
The container takes about 1-2 minutes to fully initialize. You can verify it's ready by checking the SEMP API:

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/SEMP
```

Wait until you get a 200 response code.

## Step 4: Run Setup Script
Navigate to the project directory and run the setup script:

```bash
cd /path/to/trade-ingestion
./scripts/setup-solace.sh
```

This script will:
- Create the default VPN (if it doesn't exist)
- Create necessary queues (DEAL.IN, DEAL.OUT, DEAL.DLQ)
- Set up message subscriptions
- Configure proper permissions

## Step 5: Verify Setup
You can verify the setup in two ways:

### Option 1: Using Solace PubSub+ Manager
1. Open your browser and navigate to: http://localhost:8080
2. Log in with:
   - Username: admin
   - Password: admin
3. Navigate to "Message VPNs" → "default"
4. Check that the following queues exist:
   - DEAL.IN
   - DEAL.OUT
   - DEAL.DLQ
5. Verify the subscription for DEAL.IN queue

### Option 2: Using SEMP API
```bash
# Check VPN
curl -u admin:admin http://localhost:8080/SEMP/v2/config/msgVpns/default

# Check Queues
curl -u admin:admin http://localhost:8080/SEMP/v2/config/msgVpns/default/queues

# Check Subscriptions
curl -u admin:admin http://localhost:8080/SEMP/v2/config/msgVpns/default/queues/DEAL.IN/subscriptions
```

## Troubleshooting

### Common Issues

1. **Container fails to start**
   - Check if ports are already in use
   - Verify you have enough memory and CPU resources
   - Try removing existing container: `docker rm -f solace`

2. **Setup script fails**
   - Ensure Solace is fully initialized (wait 1-2 minutes)
   - Check if the container is running: `docker ps | grep solace`
   - Verify SEMP API is accessible: `curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/SEMP`

3. **Connection issues in application**
   - Verify port mapping (55554:55555)
   - Check VPN name is "default"
   - Confirm credentials (admin/admin)

### Logs
To view Solace container logs:
```bash
docker logs solace
```

## Cleanup
To stop and remove the Solace container:
```bash
docker stop solace
docker rm solace
```

## Additional Resources
- [Solace PubSub+ Documentation](https://docs.solace.com/)
- [Solace Docker Image Documentation](https://hub.docker.com/r/solace/solace-pubsub-standard)
- [SEMP API Reference](https://docs.solace.com/API-Developer-Online-Ref-Documentation/swagger-ui/index.html)

## Support
If you encounter any issues not covered in this guide:
1. Check the troubleshooting section
2. Review the Solace container logs
3. Contact the development team 