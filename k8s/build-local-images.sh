#!/usr/bin/env bash
# Builds all images directly inside Minikube's Docker, so no registry is needed.
# Usage: ./k8s/build-local-images.sh   then   ./k8s/deploy.sh dev local latest
set -euo pipefail
eval "$(minikube docker-env)"
cd "$(dirname "$0")/.."
for svc in user-service book-service order-service frontend; do
  echo "Building $svc"
  docker build -t "local/bookstore-$svc:latest" "$svc"
done
