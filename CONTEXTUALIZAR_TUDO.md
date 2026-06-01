# CONTEXTUALIZAR TUDO: Four Fight Gym System

> Status: Legado controlado
> Documento de referencia ampla para IA e alinhamento tecnico. Para execucao operacional, usar `README.md`, `docs/DEPLOYMENT.md` e `docs/DEPLOYMENT_CHECKLIST.md`.

## 1) Visao geral

Projeto em producao da 4Four Fight Academy, nao limitado a site institucional. O sistema cobre area publica, autenticacao, area de aluno, area administrativa e base de area de professor.

URLs de producao:

- Frontend: `https://4fourfight.com`
- API: `https://api.4fourfight.com/api`
- Health: `https://api.4fourfight.com/api/health`

## 2) Stack oficial

- Frontend: React + TypeScript + Vite + Tailwind + TanStack Router/Query + Radix/shadcn-like
- Backend: Spring Boot 3.3 + Java 21 + Spring Security + JWT + Flyway
- Banco: PostgreSQL (Supabase)
- Infra: Cloudflare Pages (frontend), VPS Ubuntu Hetzner (backend), Docker Compose, Caddy, HTTPS
- Integracoes: Resend (email), Stripe (preparado para finalizacao)

## 3) Arquitetura e principios

- Backend e fonte de verdade
- Fluxo backend: Controller -> Service -> Repository -> Entity
- Frontend nao acessa Supabase direto; passa pela API
- Logica de negocio fica em Service
- Endpoints privados exigem autenticacao e autorizacao

## 4) Regras de seguranca imutaveis

Nunca enfraquecer:

- JWT authentication
- RBAC por role
- ownership checks
- rate limiting
- validacao backend
- hash de senha com BCrypt
- CORS correto
- protecao contra IDOR

Nunca fazer:

- confiar em `userId`, `role`, `plano`, `idade` ou `preco` vindos do frontend
- expor secrets ou commitar `.env`
- expor chaves de API
- usar `localStorage` para token sensivel
- abrir endpoint privado

## 5) Estado funcional atual

- Site publico online
- Login e registro ativos
- Pre-registro de alunos ativo
- Recuperacao de senha por email (Resend) ativa
- Gestao de planos e checkout/base de pagamentos ativa
- Dashboard administrativo ativo
- Estrutura para professor ativa

## 6) Correcoes criticas ja aplicadas

### 6.1 Pagamentos e autorizacao

- `GET /api/payments` agora e role-aware
- `GET /api/payments/{id}` com ownership check fortalecido
- `POST /api/payments` vinculado ao principal autenticado
- `PATCH /api/payments/{id}/complete` restrito a `ADMIN/MANAGER`

### 6.2 Registro com data de nascimento

- Frontend usa Dia/Mes/Ano e envia `dateOfBirth`
- Backend calcula idade server-side por `dateOfBirth`
- Rejeita data futura, idade fora de faixa e idade manipulada

## 7) Contexto operacional (resumo)

- Arquitetura de producao vigente: Cloudflare Pages (frontend) + VPS Ubuntu/Hetzner (backend) + Docker Compose + Caddy.
- Endpoint oficial da API: `https://api.4fourfight.com/api`.
- Variavel oficial do frontend para API: `VITE_API_URL=https://api.4fourfight.com/api`.
- Este arquivo nao e runbook operacional de deploy.

Para fluxo operacional detalhado e comandos atualizados:

- `README.md` (fonte de verdade)
- `docs/DEPLOYMENT.md`
- `docs/DEPLOYMENT_CHECKLIST.md`

## 8) Segredos e exposicao

- Segredos e credenciais nunca devem aparecer em documentacao de contexto.
- Nao incluir tokens, API keys, senhas, chaves privadas ou valores completos de `.env`.
- Em caso de necessidade de referencia, usar apenas nomes de variaveis de forma generica.

## 9) Pendencias tecnicas prioritarias

1. Confirmar limpeza final de usuario smoke de teste
2. Auditar/fechar rate limits completos
3. Aplicar cache apenas em dados publicos estaveis
4. Fechar pipeline de video (upload -> WebM -> cleanup fisico)
5. Auditar secrets/logs/headers/exposicao publica
6. Testar registro mobile com Dia/Mes/Ano em dispositivos reais

## 10) Regra de manutencao deste arquivo

- Nao duplicar runbook de deploy aqui
- Toda mudanca critica deve refletir primeiro em `README.md`
- Se houver conflito entre documentos, prevalece `README.md` + docs operacionais
