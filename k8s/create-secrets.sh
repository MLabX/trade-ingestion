#!/bin/bash

# Create Kubernetes secrets for the Deal Ingestion service
# Usage: ./create-secrets.sh <namespace>

NAMESPACE=${1:-default}

echo "Creating secrets in namespace: $NAMESPACE"

# Create the secrets
kubectl create secret generic deal-ingestion-secrets \
  --namespace=$NAMESPACE \
  --from-literal=db_user=postgres \
  --from-literal=db_password=postgres \
  --from-literal=solace_username=default \
  --from-literal=solace_password=default

echo "Secrets created successfully" 