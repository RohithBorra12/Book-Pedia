#!/usr/bin/env bash
# Creates one ECR repository per image and logs Docker in to ECR.
# Usage: ./aws/setup-ecr.sh ap-south-1
set -euo pipefail
REGION=${1:-ap-south-1}
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
REGISTRY="$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com"

for svc in user-service book-service order-service frontend; do
  aws ecr describe-repositories --repository-names "bookstore-$svc" --region "$REGION" >/dev/null 2>&1 \
    || aws ecr create-repository --repository-name "bookstore-$svc" --region "$REGION" >/dev/null
  echo "Repository ready: $REGISTRY/bookstore-$svc"
done

aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "$REGISTRY"
echo
echo "Your registry is: $REGISTRY"
