# Four Fight Gym System - Deployment Checklist

> Status: Ativo
> Fonte canonica: `README.md`
> Ordem de leitura: `docs/INDEX.md`

## 1) Pre-deploy

- [ ] Revisar diff e garantir que nao ha secrets, `.env` ou `.pem` staged
- [ ] Confirmar arquitetura atual (Cloudflare Pages + VPS + Docker Compose + Caddy)
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
git pull origin main
cd /opt/fourfight
docker compose up -d --build
sleep 25
curl -i http://127.0.0.1:10000/api/health
curl -i https://api.4fourfight.com/api/health
```

- [ ] Health interno `200`
- [ ] Health externo `200`
- [ ] Containers `fourfight-backend` e `fourfight-caddy` ativos

## 4) Pos-deploy minimo

- [ ] `GET https://api.4fourfight.com/api/plans` responde sem erro
- [ ] Login/registro continuam funcionais
- [ ] Endpoint privado sem token continua nao autorizado

## 5) Troubleshooting

- [ ] Se houver `502` inicial, aguardar 20-30s e testar health novamente
- [ ] Se env mudar e nao refletir: `docker compose up -d --force-recreate backend`
