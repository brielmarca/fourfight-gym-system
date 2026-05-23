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

RUN_PROFILES="${APP_PROFILES:-admin-promote}"

if [[ "${LIST_USERS_ONLY:-}" == "true" ]]; then
  mvn -q -DskipTests -Dspring-boot.run.profiles="$RUN_PROFILES" spring-boot:run
  exit 0
fi

if [[ -z "${PROMOTE_ADMIN_EMAIL:-}" || "${CONFIRM_PROMOTE_ADMIN:-}" != "true" ]]; then
  echo "Required env vars: PROMOTE_ADMIN_EMAIL and CONFIRM_PROMOTE_ADMIN=true"
  exit 1
fi

mvn -q -DskipTests -Dspring-boot.run.profiles="$RUN_PROFILES" spring-boot:run
