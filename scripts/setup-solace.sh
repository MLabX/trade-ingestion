#!/bin/bash

# Solace setup script using SEMP API
SOLACE_HOST="localhost"
SOLACE_PORT="8080"
SOLACE_USERNAME="admin"
SOLACE_PASSWORD="admin"
VPN_NAME="default"

# Function to make SEMP API calls
semp_api() {
    local method=$1
    local path=$2
    local data=$3
    
    curl -s -X $method \
        -H "Content-Type: application/json" \
        -u "$SOLACE_USERNAME:$SOLACE_PASSWORD" \
        "http://$SOLACE_HOST:$SOLACE_PORT/SEMP/v2/config$path" \
        -d "$data"
}

# Function to check if VPN exists
check_vpn_exists() {
    local response=$(semp_api "GET" "/msgVpns/$VPN_NAME" "")
    if [[ $response == *"\"msgVpnName\":\"$VPN_NAME\""* ]]; then
        return 0
    else
        return 1
    fi
}

# Function to check if queue exists
check_queue_exists() {
    local queue_name=$1
    local response=$(semp_api "GET" "/msgVpns/$VPN_NAME/queues/$queue_name" "")
    if [[ $response == *"\"queueName\":\"$queue_name\""* ]]; then
        return 0
    else
        return 1
    fi
}

# Function to check if subscription exists
check_subscription_exists() {
    local queue_name=$1
    local topic=$2
    local response=$(semp_api "GET" "/msgVpns/$VPN_NAME/queues/$queue_name/subscriptions/$topic" "")
    if [[ $response == *"\"subscriptionTopic\":\"$topic\""* ]]; then
        return 0
    else
        return 1
    fi
}

# Wait for Solace to be ready
echo "Waiting for Solace to be ready..."
while ! curl -s -o /dev/null -w "%{http_code}" http://$SOLACE_HOST:$SOLACE_PORT/SEMP > /dev/null; do
    echo "Solace not ready yet, waiting..."
    sleep 5
done

# Check if VPN exists
echo "Checking VPN configuration..."
if ! check_vpn_exists; then
    echo "Creating VPN..."
    semp_api "POST" "/msgVpns" "{
        \"msgVpnName\": \"$VPN_NAME\",
        \"enabled\": true
    }"
else
    echo "VPN '$VPN_NAME' already exists"
fi

# Create queues
echo "Creating queues..."
for queue in "DEAL.DLQ" "DEAL.IN" "DEAL.OUT"; do
    if ! check_queue_exists "$queue"; then
        echo "Creating queue: $queue"
        semp_api "POST" "/msgVpns/$VPN_NAME/queues" "{
            \"queueName\": \"$queue\",
            \"accessType\": \"exclusive\",
            \"permission\": \"consume\",
            \"ingressEnabled\": true,
            \"egressEnabled\": true,
            \"respectTtlEnabled\": true,
            \"maxMsgSize\": 10000000,
            \"maxMsgSpoolUsage\": 1000
        }"
    else
        echo "Queue '$queue' already exists"
    fi
done

# Create subscription for DEAL.IN queue if it doesn't exist
echo "Checking DEAL.IN subscription..."
if ! check_subscription_exists "DEAL.IN" "DEAL.IN"; then
    echo "Creating subscription for DEAL.IN queue..."
    semp_api "POST" "/msgVpns/$VPN_NAME/queues/DEAL.IN/subscriptions" "{
        \"subscriptionTopic\": \"DEAL.IN\"
    }"
else
    echo "Subscription for DEAL.IN already exists"
fi

echo "Solace setup completed successfully!" 