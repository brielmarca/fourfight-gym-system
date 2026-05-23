#!/usr/bin/env bash
set -euo pipefail

if [[ "${LIST_ADMINS_ONLY:-}" == "true" ]]; then
  mvn -q -DskipTests -Dspring-boot.run.profiles=password-reset spring-boot:run
  exit 0
fi

if [[ -z "${ADMIN_EMAIL:-}" || -z "${ADMIN_NEW_PASSWORD:-}" ]]; then
  echo "Required env vars: ADMIN_EMAIL and ADMIN_NEW_PASSWORD"
  exit 1
fi

mvn -q -DskipTests -Dspring-boot.run.profiles=password-reset spring-boot:run
