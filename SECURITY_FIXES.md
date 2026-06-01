# Security Fixes - Audit Trail

> Status: Ativo
> Natureza: historico de hardening e auditoria
> Fonte operacional de deploy: `README.md` e `docs/DEPLOYMENT.md`

## Escopo

Este documento registra correcoes de seguranca aplicadas e decisoes de hardening.
Nao e um runbook de deploy.

## Baseline de seguranca (vigente)

- Autenticacao JWT e autorizacao por RBAC
- Ownership checks para recursos sensiveis
- Politica anti-enumeration em recursos by-id sensiveis (`404` para nao-owner autenticado)
- Hash de senha com BCrypt
- Rate limiting em fluxos sensiveis
- HTTPS via proxy reverso (operacao atual: Caddy)

## Registro de hardening recente (2026-06-01)

- `3444845` - hardening de membership e ownership access controls
  - listagem de memberships restrita a `ADMIN`/`MANAGER`
  - ownership checks reforcados em fluxos sensiveis

- `d841605` - regressao de schedule requests
  - cobertura de seguranca para ownership em endpoints por id

- `eafb45a` - ownership de notifications
  - operacoes by-id escopadas ao usuario autenticado
  - nao-owner autenticado retorna `404`

## Preflight audit (historico)

- Data: 2026-06-01
- Branch: `security/preflight-audit-2026-06-01`
- Resultado: baseline de seguranca preservado; conflitos documentais identificados e tratados em docs de deploy

## Nota sobre proxy (historico x atual)

- Historico: parte do hardening original citava Nginx.
- Operacao atual: proxy reverso e HTTPS com Caddy na VPS.

## Proxima revisao

- Trigger: qualquer mudanca em auth, autorizacao, endpoints sensiveis, deploy, headers, ou pagamentos.
