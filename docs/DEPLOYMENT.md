# Deployment Guide - Four Fight Gym System

> Status: Ativo
> Fonte canonica: `README.md`
> Ordem de leitura: `docs/INDEX.md`

## Producao atual

- Frontend: Cloudflare Pages (`https://4fourfight.com`)
- API: VPS Ubuntu/Hetzner + Docker Compose + Caddy (`https://api.4fourfight.com/api`)
- Health: `https://api.4fourfight.com/api/health`

## Regras operacionais

- Mudanca so no frontend: deploy Cloudflare Pages, sem operacao na VPS.
- Mudanca no backend: atualizar repo na VPS e recriar stack com Docker Compose.
- `README.md` prevalece em caso de conflito.

## Deploy frontend (Cloudflare Pages)

- Trigger: push para `main`.
- Variavel obrigatoria: `VITE_API_URL=https://api.4fourfight.com/api`.
- Nao usar `VITE_API_BASE_URL`.

Validacao minima:

```bash
curl -I https://4fourfight.com
```

## Deploy backend (VPS)

Backend rate limiting trusts `X-Forwarded-For`/`X-Real-IP` only when the immediate sender matches `RATE_LIMIT_TRUSTED_PROXIES`. Spring framework-level forwarded-header transformation is disabled in production so the rate-limit resolver can make the trust decision from the immediate peer address. Keep `RATE_LIMIT_TRUSTED_PROXIES` empty by default, and set it only to the actual reverse proxy container/host IP or CIDR after confirming the backend is not publicly reachable directly.

```bash
ssh <deploy-user>@<vps-host>
cd /opt/fourfight/fourfight-gym-system
git pull origin main
cd /opt/fourfight
docker compose up -d --build
sleep 25
curl -i http://127.0.0.1:10000/api/health
curl -i https://api.4fourfight.com/api/health
```

Se houver `502` imediatamente apos o deploy, aguardar 20-30s e repetir os checks de health.

## Verificacoes pos-deploy

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
curl -i https://api.4fourfight.com/api/health
curl -i https://api.4fourfight.com/api/plans
```

Esperado:

- Containers ativos: `fourfight-backend`, `fourfight-caddy`
- Health externo retorna `200`
- Endpoint publico de planos responde sem erro

## Troubleshooting rapido

- `health interno falha`: backend nao subiu corretamente
- `health externo falha e interno ok`: revisar proxy/rede (Caddy)
- frontend sem dados: revisar `VITE_API_URL` no Cloudflare

## Nota historica

- Referencias antigas a Render/Nginx devem ser tratadas apenas como historico, nao como runbook operacional atual.
