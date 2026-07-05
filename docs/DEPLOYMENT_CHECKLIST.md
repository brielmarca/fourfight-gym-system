# Four Fight Gym System - Deployment Checklist

> Status: Ativo
> Fonte canonica: `README.md`
> Ordem de leitura: `docs/INDEX.md`

## 1) Pre-deploy

- [ ] Revisar diff e garantir que nao ha secrets, `.env` ou `.pem` staged
- [ ] Confirmar arquitetura atual (Cloudflare Pages + VPS + Docker Compose + Caddy)
- [ ] Confirmar alvo de producao `root@178.105.215.50`
- [ ] Confirmar diretorio de producao `/opt/fourfight`
- [ ] Confirmar Compose de producao `/opt/fourfight/docker-compose.yml`, projeto `fourfight`, servico `backend`, container `fourfight-backend`
- [ ] Confirmar banco externo via `/opt/fourfight/.env`, sem usar env local do repo
- [ ] Confirmar frontend com `VITE_API_URL=https://api.4fourfight.com/api`

## 2) Frontend-only change

- [ ] Push para `main`
- [ ] Aguardar deploy no Cloudflare Pages
- [ ] Validar `https://4fourfight.com`
- [ ] Validar ausencia de erro de CORS no navegador
- [ ] Nao executar fluxo de VPS

## 3) Backend-including change

Executar na VPS:

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

- [ ] Health interno `200`
- [ ] Health externo `200`
- [ ] Containers `fourfight-backend` e `fourfight-caddy` ativos
- [ ] `docker compose config --services` lista `backend` e `caddy`, sem `app`, `postgres`, `redis` ou `nginx`

## 4) Pos-deploy minimo

- [ ] `GET https://api.4fourfight.com/api/plans` responde sem erro
- [ ] Login/registro continuam funcionais
- [ ] Endpoint privado sem token continua nao autorizado

## 4.1) Follow-up pos-deploy autenticado (quando houver credenciais aprovadas)

- [ ] Executar apenas com credenciais de producao aprovadas para smoke test
- [ ] Validar login
- [ ] Validar fluxo de refresh token
- [ ] Validar registro somente se existir processo aprovado para smoke-user
- [ ] Validar acesso de `ADMIN`/`MANAGER` a listagem de memberships
- [ ] Validar que `CLIENT` nao consegue listar todos os memberships
- [ ] Validar que `CLIENT` consegue acessar apenas o proprio membership
- [ ] Validar que owner de notification acessa notificacao propria
- [ ] Validar que non-owner em notification by-id recebe `404`
- [ ] Validar comportamento owner/non-owner em schedule requests
- [ ] Evitar testes agressivos de rate limit em producao
- [ ] Nao expor credenciais, tokens ou cookies em logs/screenshots
- [ ] Nao modificar dados de producao fora de dados de smoke test aprovados

## 5) Troubleshooting

- [ ] Se houver `502` inicial, aguardar 20-30s e testar health novamente
- [ ] Se env mudar e nao refletir: `docker compose up -d --force-recreate backend`
