# Deployment Guide - Four Fight Gym System

> Status: Active
> Canonical production runtime: `/opt/fourfight`

## Production

- Target: `root@178.105.215.50`
- Deployment directory: `/opt/fourfight`
- Compose file: `/opt/fourfight/docker-compose.yml`
- Compose project: `fourfight`
- Compose service: `backend`
- Container: `fourfight-backend`
- Reverse proxy: Caddy (`fourfight-caddy`)
- Database: external PostgreSQL through `DATABASE_URL`
- Environment file: `/opt/fourfight/.env`
- Public API: `https://api.4fourfight.com/api`
- Public health: `https://api.4fourfight.com/api/health`

Backend rate limiting trusts `X-Forwarded-For`/`X-Real-IP` only when the immediate sender matches `RATE_LIMIT_TRUSTED_PROXIES`. Keep `RATE_LIMIT_TRUSTED_PROXIES` empty by default, and set it only to the actual reverse proxy container/host IP or CIDR after confirming the backend is not publicly reachable directly.

Production backend update command:

```bash
ssh root@178.105.215.50
cd /opt/fourfight/fourfight-gym-system
git pull --ff-only origin main
cd /opt/fourfight
docker compose config --services
docker compose up --build -d backend
sleep 25
curl -i http://127.0.0.1:10000/api/health
curl -i https://api.4fourfight.com/api/health
```

Expected production services from `/opt/fourfight`:

```bash
docker compose config --services
```

Expected output:

```text
backend
caddy
```

If there is an immediate `502`, wait 20-30 seconds and repeat the health checks.

## Development

The development Compose file is intentionally explicit and development-only:

- File: `backend/docker-compose.dev.yml`
- Project name: `fourfight-dev`
- Services: `backend-dev`, `postgres-dev`, `redis-dev`
- Database: local Docker PostgreSQL volume `fourfight-dev-postgres-data`
- Redis: local Docker Redis volume `fourfight-dev-redis-data`
- Reverse proxy: none; this does not use production Caddy or legacy nginx
- Environment template: `backend/docker-compose.dev.env.example`

Development startup:

```bash
cd backend
cp docker-compose.dev.env.example docker-compose.dev.env
docker compose -f docker-compose.dev.yml up --build
```

Development teardown without deleting data:

```bash
cd backend
docker compose -f docker-compose.dev.yml down
```

Development volume cleanup, only after confirming local data is disposable:

```bash
cd backend
docker compose -f docker-compose.dev.yml down --volumes
```

The non-Docker local workflow remains supported through the root npm scripts:

```bash
npm run dev:full
```

## Never Do This In Production

- Do not run Docker Compose from the repository `backend/` subdirectory.
- Do not run or recreate the legacy path `backend/docker-compose.yml`.
- Do not create `backend/.env` on production.
- Do not start local `postgres-dev`, `redis-dev`, or legacy `nginx` on production.
- Do not use service `app`; production service is `backend`.
- Do not run production deploys with a default Compose file from `backend/`.
- Do not assume `localhost:8080` is the production health endpoint; production binds backend health on `127.0.0.1:10000` and public health at `https://api.4fourfight.com/api/health`.

## Notes

- The production Compose file is currently maintained on the VPS at `/opt/fourfight/docker-compose.yml`, not version-controlled in this repository.
- Do not copy production secrets into Git.
- Historical Render or nginx references are not the current production runbook.
