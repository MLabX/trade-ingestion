#!/bin/bash

# Check if Solace is running and healthy
check_solace() {
    echo "Checking Solace status..."
    
    # Check if Solace container is running
    if ! docker ps | grep -q solace; then
        echo "Solace container is not running. Starting it..."
        docker-compose up -d solace
        sleep 30  # Wait for Solace to initialize
    fi
    
    # Check Solace health
    if curl -s -f http://localhost:8080/SEMP > /dev/null; then
        echo "Solace is healthy and ready"
        return 0
    else
        echo "Solace is not healthy"
        return 1
    fi
}

# Main execution
if check_solace; then
    echo "Solace is ready for integration tests"
    exit 0
else
    echo "Solace is not ready. Please check the Solace container logs"
    exit 1
fi 