#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

fail() {
  printf '%s\n' "ERROR: $1" >&2
  exit 1
}

reject_non_warning_matches() {
  local pattern="$1"
  local message="$2"
  shift 2

  local matches
  matches="$(grep -RIn --exclude=check-compose-safety.sh --exclude-dir=.git --exclude-dir=node_modules --exclude-dir=target -E "$pattern" "$@" 2>/dev/null || true)"
  matches="$(printf '%s\n' "$matches" | grep -viE 'do not|never|nao|não|sem usar|must not' || true)"
  [ -z "$matches" ] || fail "$message"
}

for default_compose in \
  backend/docker-compose.yml \
  backend/docker-compose.yaml \
  backend/compose.yml \
  backend/compose.yaml
do
  [ ! -e "$default_compose" ] || fail "$default_compose must not exist; use backend/docker-compose.dev.yml for local development"
done

[ -f backend/docker-compose.dev.yml ] || fail "missing backend/docker-compose.dev.yml"
[ -f docs/DEPLOYMENT.md ] || fail "missing docs/DEPLOYMENT.md"

grep -q 'service: `backend`' docs/DEPLOYMENT.md || fail "production deployment docs must identify service backend"
grep -q 'docker compose up --build -d backend' docs/DEPLOYMENT.md || fail "production deployment command must target backend only"
grep -q '^name: fourfight-dev$' backend/docker-compose.dev.yml || fail "development Compose must use explicit name: fourfight-dev"
grep -q 'DEVELOPMENT ONLY' backend/docker-compose.dev.yml || fail "development Compose must include a visible development-only warning"

reject_non_warning_matches 'cd backend.*docker compose up|docker compose up.*app|docker compose up.*postgres|docker compose up.*redis|docker compose up.*nginx' "unsafe production-like Compose command found" README.md docs scripts backend
reject_non_warning_matches 'docker compose up .*app|docker compose up .*postgres|docker compose up .*redis|docker compose up .*nginx' "production documentation must not deploy app/postgres/redis/nginx" README.md docs
reject_non_warning_matches '(^|[^A-Za-z])service:?[[:space:]]+`?app`?|docker compose up .* app' "production documentation must not use service app" README.md docs
reject_non_warning_matches '^(cp|touch|source|export|[[:space:]]*) .*backend/\.env|backend/\.env .*production env|production env.*backend/\.env' "production documentation must not use backend/.env" README.md docs scripts
reject_non_warning_matches 'production.*localhost:8080|localhost:8080.*production health|localhost:8080.*produc' "production documentation must not use localhost:8080 as health" README.md docs

docker compose -f backend/docker-compose.dev.yml config --services >/dev/null

printf '%s\n' "Compose safety checks passed."
