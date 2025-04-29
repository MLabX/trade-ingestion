#!/bin/bash

# Configuration
HOST="localhost"
SEMP_PORT="8080"
VPN_NAME="default"
ADMIN_USER="admin"
ADMIN_PASSWORD="admin"
BASE_URL="http://${HOST}:${SEMP_PORT}/SEMP/v2/config"

# Required queues and their configurations
declare -A QUEUES=(
    ["DEAL.IN"]="{\"queueName\":\"DEAL.IN\",\"accessType\":\"exclusive\",\"permission\":\"consume\",\"ingressEnabled\":true,\"egressEnabled\":true,\"respectTtlEnabled\":true,\"maxMsgSize\":10000000,\"maxMsgSpoolUsage\":500}"
    ["DEAL.OUT"]="{\"queueName\":\"DEAL.OUT\",\"accessType\":\"exclusive\",\"permission\":\"consume\",\"ingressEnabled\":true,\"egressEnabled\":true,\"respectTtlEnabled\":true,\"maxMsgSize\":10000000,\"maxMsgSpoolUsage\":500}"
    ["DEAL.DLQ"]="{\"queueName\":\"DEAL.DLQ\",\"accessType\":\"exclusive\",\"permission\":\"consume\",\"ingressEnabled\":true,\"egressEnabled\":true,\"respectTtlEnabled\":true,\"maxMsgSize\":10000000,\"maxMsgSpoolUsage\":500}"
)

# Required subscriptions for each queue
declare -A SUBSCRIPTIONS=(
    ["DEAL.IN"]="DEAL.IN"
    ["DEAL.OUT"]="DEAL.OUT"
    ["DEAL.DLQ"]="DEAL.DLQ"
)

# Wait for SEMP API to be ready
echo "Waiting for SEMP API to be ready..."
until curl -s -u "${ADMIN_USER}:${ADMIN_PASSWORD}" "${BASE_URL}/about/api" > /dev/null; do
    echo "SEMP API not ready yet, waiting..."
    sleep 5
done
echo "SEMP API is ready!"

# Configure VPN
echo "Configuring VPN..."
curl -s -X POST \
    -u "${ADMIN_USER}:${ADMIN_PASSWORD}" \
    -H "Content-Type: application/json" \
    "${BASE_URL}/msgVpns" \
    -d "{\"msgVpnName\":\"${VPN_NAME}\",\"enabled\":true}"

# Configure queues
echo "Configuring queues..."
for queue in "${!QUEUES[@]}"; do
    echo "Creating queue: ${queue}"
    curl -s -X POST \
        -u "${ADMIN_USER}:${ADMIN_PASSWORD}" \
        -H "Content-Type: application/json" \
        "${BASE_URL}/msgVpns/${VPN_NAME}/queues" \
        -d "${QUEUES[$queue]}"
done

# Configure subscriptions
echo "Configuring subscriptions..."
for queue in "${!SUBSCRIPTIONS[@]}"; do
    echo "Creating subscription for queue ${queue} to topic ${SUBSCRIPTIONS[$queue]}"
    curl -s -X POST \
        -u "${ADMIN_USER}:${ADMIN_PASSWORD}" \
        -H "Content-Type: application/json" \
        "${BASE_URL}/msgVpns/${VPN_NAME}/queues/${queue}/subscriptions" \
        -d "{\"subscriptionTopic\":\"${SUBSCRIPTIONS[$queue]}\"}"
done

# Configure client profiles
echo "Configuring client profiles..."
curl -s -X POST \
    -u "${ADMIN_USER}:${ADMIN_PASSWORD}" \
    -H "Content-Type: application/json" \
    "${BASE_URL}/msgVpns/${VPN_NAME}/clientProfiles" \
    -d '{"clientProfileName":"default","allowGuaranteedMsgSendEnabled":true,"allowGuaranteedMsgReceiveEnabled":true,"allowGuaranteedEndpointCreateEnabled":true,"allowTransactedSessionsEnabled":true}'

# Configure client usernames
echo "Configuring client usernames..."
curl -s -X POST \
    -u "${ADMIN_USER}:${ADMIN_PASSWORD}" \
    -H "Content-Type: application/json" \
    "${BASE_URL}/msgVpns/${VPN_NAME}/clientUsernames" \
    -d '{"clientUsername":"test-client","enabled":true,"password":"test-password","clientProfileName":"default"}'

echo "Solace test setup completed!" 