# Four Fight Gym System

Plataforma web completa da 4Four Fight Academy, com site publico, area de aluno, area administrativa, autenticacao JWT, gestao de planos, base para pagamentos e operacao em producao.

## Producao atual

- Frontend: `https://4fourfight.com`
- API: `https://api.4fourfight.com/api`
- Health check: `https://api.4fourfight.com/api/health`
- Infra principal: Cloudflare Pages (frontend) + VPS Ubuntu/Hetzner (backend) + Docker Compose + Caddy

## Escopo funcional

- Site institucional da academia
- Login, registro, pre-registro e recuperacao de senha por email
- Area de aluno
- Area administrativa
- Estrutura de area de professor
- Gestao de planos e base para fluxo de pagamentos
- Integracoes operacionais (WhatsApp, email, paginas legais)

## Stack oficial

- Frontend: React, TypeScript, Vite, Tailwind, TanStack Router/Query, Radix/shadcn-like, Framer Motion
- Backend: Spring Boot 3.3, Java 21, Spring Security, JWT, Flyway
- Banco: PostgreSQL (Supabase)
- Infra: Docker Compose, Caddy reverse proxy, HTTPS, Cloudflare
- Email transacional: Resend

## Arquitetura e regras imutaveis

- Backend e a fonte de verdade; frontend nao decide role, ownership, preco, plano ou idade
- Fluxo backend: Controller -> Service -> Repository -> Entity
- Nao confiar em `userId` vindo do frontend para operacoes sensiveis
- Nao expor segredos, nao commitar `.env`, nao commitar chaves `.pem`
- Endpoints privados devem permanecer protegidos por JWT + RBAC + ownership checks
- Manter rate limiting nos endpoints de autenticacao e fluxos sensiveis

## Deploy

### Frontend

- Plataforma: Cloudflare Pages
- Deploy: automatico em push para `main`
- Variavel obrigatoria: `VITE_API_URL=https://api.4fourfight.com/api`
- Nao usar `VITE_API_BASE_URL`

### Backend

- Host: VPS Ubuntu (`<vps-host>`)
- Repo na VPS: `/opt/fourfight/fourfight-gym-system`
- Stack runtime: `/opt/fourfight` com `docker compose`

Fluxo de atualizacao backend:

```bash
ssh <deploy-user>@<vps-host>
cd /opt/fourfight/fourfight-gym-system
git pull --ff-only origin main
cd /opt/fourfight
docker compose config --services
docker compose up --build -d backend
sleep 25
curl -i http://127.0.0.1:10000/api/health
curl -i https://api.4fourfight.com/api/health
```

Observacao: `502` logo apos deploy pode ser apenas boot do backend; aguardar 20-30s e testar novamente.

## Execucao local

Pre-requisitos:

- Java 21+
- Node.js 18+
- Maven 3.8+

Comandos:

```bash
npm install
npm run dev:full
```

Docker Compose local development is explicitly opt-in and development-only:

```bash
cd backend
cp docker-compose.dev.env.example docker-compose.dev.env
docker compose -f docker-compose.dev.yml up --build
```

Do not run Docker Compose from `backend/` in production. Production runs from `/opt/fourfight` and targets service `backend`.

Ou separado:

```bash
npm run dev:backend
npm run dev:frontend
```

## Operacao segura de repositorio

- Revisar staged files antes de commit
- Nao usar `git add .` sem verificacao
- Garantir `.gitignore` cobrindo segredos e artefatos locais
- Se alguma chave privada ja entrou em historico, rotacionar imediatamente

## Documentacao oficial

Ordem e status da documentacao em `docs/INDEX.md`.

Leitura recomendada:

1. `docs/INDEX.md`
2. `docs/DEPLOYMENT.md`
3. `docs/DEPLOYMENT_CHECKLIST.md`
4. `SECURITY_FIXES.md` (quando aplicavel a hardening)
5. `CONTEXTUALIZAR_TUDO.md`

## Nota de manutencao

Este README e documento-mestre. Qualquer arquivo `.md` novo deve evitar duplicacao e apontar para a fonte canonica no `docs/INDEX.md`.
