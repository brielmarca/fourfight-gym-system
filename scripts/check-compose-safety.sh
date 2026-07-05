#!/bin/sh
set -eu

fail() {
  printf '%s\n' "ERROR: $1" >&2
  exit 1
}

[ ! -e backend/docker-compose.yml ] || fail "backend/docker-compose.yml must not exist; use backend/docker-compose.dev.yml for local development"
[ -f backend/docker-compose.dev.yml ] || fail "missing backend/docker-compose.dev.yml"
[ -f docs/DEPLOYMENT.md ] || fail "missing docs/DEPLOYMENT.md"

grep -q 'service: `backend`' docs/DEPLOYMENT.md || fail "production deployment docs must identify service backend"
grep -q 'docker compose up --build -d backend' docs/DEPLOYMENT.md || fail "production deployment command must target backend only"
! grep -RIn --exclude=check-compose-safety.sh --exclude-dir=.git --exclude-dir=node_modules --exclude-dir=target 'cd backend.*docker compose up\|docker compose up.*app\|docker compose up.*postgres\|docker compose up.*nginx' README.md docs scripts backend 2>/dev/null || fail "unsafe production-like Compose command found"

docker compose -f backend/docker-compose.dev.yml config --services >/dev/null

printf '%s\n' "Compose safety checks passed."
