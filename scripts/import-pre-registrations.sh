#!/usr/bin/env bash
set -euo pipefail

CSV_PATH="${1:-./imports/4Four Fight Academy (respostas) - Respostas ao formulário 1.csv}"
API_BASE="${API_BASE:-http://localhost:8080/api}"
ADMIN_EMAIL="${ADMIN_EMAIL:-}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-}"

if [[ -z "$ADMIN_EMAIL" || -z "$ADMIN_PASSWORD" ]]; then
  echo "Set ADMIN_EMAIL and ADMIN_PASSWORD before running."
  exit 1
fi

if [[ ! -f "$CSV_PATH" ]]; then
  echo "CSV file not found: $CSV_PATH"
  exit 1
fi

LOGIN_RESPONSE="$(curl -sS -X POST "$API_BASE/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}")"
ACCESS_TOKEN="$(printf '%s' "$LOGIN_RESPONSE" | python3 -c 'import json,sys; print(json.load(sys.stdin).get("accessToken",""))')"

if [[ -z "$ACCESS_TOKEN" ]]; then
  echo "Login failed. Could not obtain access token."
  exit 1
fi

curl -sS -X POST "$API_BASE/admin/pre-registrations/import" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -F "file=@$CSV_PATH"

echo
