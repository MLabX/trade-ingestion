#!/bin/bash

# Stop and remove any existing container
docker stop solace-test || true
docker rm solace-test || true

# Check if ports are available
echo "Checking if required ports are available..."
for port in 8080 55556 8008 5672 1883 9000; do
    if lsof -i :$port > /dev/null; then
        echo "Error: Port $port is already in use"
        exit 1
    fi
done

# Start Solace container
docker run -d \
--name solace-test \
-p 8080:8080 \
-p 55556:55555 \
-p 8008:8008 \
-p 5672:5672 \
-p 1883:1883 \
-p 9000:9000 \
-e username_admin_globalaccesslevel=admin \
-e username_admin_password=admin \
-e system_scaling_maxconnectioncount=100 \
-e system_scaling_maxqueuesize=500 \
-e service_semp_plaintext_port=8080 \
-e service_smf_port=55556 \
-e service_smf_compressed_port=55003 \
-e service_web_transport_port=8008 \
--shm-size=2g \
solace/solace-pubsub-standard:latest

echo "Solace test container started. Waiting for it to be ready..."

# Wait for container to start
echo "Waiting for Solace test container to start..."
sleep 30

# Run test setup script
echo "Running Solace test setup..."
./scripts/setup-solace-test.sh

echo "Solace test container is ready!" 