#!/bin/bash

# Check if Solace is reachable on port 55556
if ! nc -z -w5 localhost 55556; then
    echo "Error: Solace is not reachable on port 55556"
    echo "Please ensure Solace is running before starting the application"
    exit 1
fi

echo "Solace is reachable on port 55556"
exit 0 