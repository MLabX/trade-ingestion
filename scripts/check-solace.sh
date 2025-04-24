#!/bin/bash

# Check if Solace is reachable on port 55555
if ! nc -z -w5 localhost 55555; then
    echo "Error: Solace is not reachable on port 55555"
    exit 1
fi

echo "Solace is reachable on port 55555"
exit 0 