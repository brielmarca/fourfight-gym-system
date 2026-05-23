#!/usr/bin/env bash
set -euo pipefail

if [[ -f ".env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source ".env"
  set +a
fi

if [[ -f ".env.local" ]]; then
  set -a
  # shellcheck disable=SC1091
  source ".env.local"
  set +a
fi

RUN_PROFILES="${APP_PROFILES:-admin-bootstrap}"

if [[ -z "${ADMIN_NAME:-}" || -z "${ADMIN_EMAIL:-}" || -z "${ADMIN_NEW_PASSWORD:-}" || "${CONFIRM_CREATE_ADMIN:-}" != "true" ]]; then
  echo "Required env vars: ADMIN_NAME, ADMIN_EMAIL, ADMIN_NEW_PASSWORD, CONFIRM_CREATE_ADMIN=true"
  exit 1
fi

mvn -q -DskipTests -Dspring-boot.run.profiles="$RUN_PROFILES" spring-boot:run
