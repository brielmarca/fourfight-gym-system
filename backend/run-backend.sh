#!/bin/bash
# Local development helper — uses values from backend/.env
set -a
source "$(dirname "$0")/.env" 2>/dev/null || true
set +a
exec java -jar target/gym-management-backend-1.0.0.jar --spring.profiles.active=dev-h2
