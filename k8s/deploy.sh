#!/usr/bin/env bash
# Usage: ./k8s/deploy.sh <env> <registry> <tag>
#   ./k8s/deploy.sh dev  local latest                                          (Minikube)
#   ./k8s/deploy.sh prod 123456789012.dkr.ecr.ap-south-1.amazonaws.com 42      (AWS EKS)
set -euo pipefail

ENV=${1:-dev}
REGISTRY=${2:-local}
TAG=${3:-latest}
NAMESPACE="bookstore-$ENV"
DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Deploying tag $TAG from $REGISTRY to namespace $NAMESPACE"

# Each environment gets its own namespace: bookstore-dev, bookstore-prod
kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -

# apps.yaml contains the Secret that infra.yaml needs, so apply it first
sed -e "s|IMAGE_REGISTRY|$REGISTRY|g" -e "s|IMAGE_TAG|$TAG|g" "$DIR/apps.yaml" | kubectl apply -n "$NAMESPACE" -f -
kubectl apply -n "$NAMESPACE" -f "$DIR/infra.yaml"

kubectl rollout status deployment/order-service -n "$NAMESPACE" --timeout=300s || true
kubectl get pods,svc -n "$NAMESPACE"
