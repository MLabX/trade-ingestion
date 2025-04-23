#!/bin/bash

# Create Kubernetes secrets for sensitive configuration
kubectl create secret generic trade-ingestion-secrets \
  --from-literal=db_name=trades \
  --from-literal=db_user=admin \
  --from-literal=db_password=secret123 \
  --from-literal=solace_username=solace_user \
  --from-literal=solace_password=solace_pass

echo "Secrets created successfully" 