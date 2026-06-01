# Documentacao Oficial - Ordem e Governanca

Este arquivo define a ordem de leitura, a funcao de cada documento e qual arquivo e a fonte canonica de cada tema.

## Objetivo

- Evitar duplicacao de conteudo entre arquivos `.md`
- Manter um unico ponto de verdade por assunto
- Facilitar onboarding, manutencao e operacao em producao

## Ordem de leitura recomendada

1. `README.md` - Visao executiva e operacao essencial
2. `docs/DEPLOYMENT.md` - Guia de deploy e infraestrutura
3. `docs/DEPLOYMENT_CHECKLIST.md` - Checklist operacional de release
4. `SECURITY_FIXES.md` - Hardening e correcoes de seguranca ja aplicadas
5. `docs/DEV_SETUP.md` - Setup local para desenvolvimento
6. `docs/STRIPE_INTEGRATION.md` - Integracao de pagamentos (quando aplicavel)
7. `CONTEXTUALIZAR_TUDO.md` - Contexto tecnico consolidado para referencia
8. `docs/TECH_DECISION_TEMPLATE.md` - Template para registrar decisoes tecnicas
9. `frontend/README.md` - Guia tecnico do frontend

## Status atual dos documentos

| Documento | Funcao principal | Status | Regra de uso |
|---|---|---|---|
| `README.md` | Documento-mestre do projeto | Ativo | Nao duplicar detalhe operacional extenso |
| `docs/DEPLOYMENT.md` | Deploy e arquitetura de entrega | Revisar | Atualizar sempre que fluxo de deploy mudar |
| `docs/DEPLOYMENT_CHECKLIST.md` | Passos de release e validacao | Revisar | Usar em toda mudanca de producao |
| `SECURITY_FIXES.md` | Registro de hardening aplicado | Ativo | Nao usar como politica unica de seguranca |
| `docs/DEV_SETUP.md` | Setup local | Ativo | Manter alinhado com scripts reais |
| `docs/STRIPE_INTEGRATION.md` | Fluxo tecnico Stripe | Ativo | Alterar apenas com dados oficiais da empresa |
| `CONTEXTUALIZAR_TUDO.md` | Contexto amplo de referencia | Legado controlado | Referencia, nao runbook |
| `docs/TECH_DECISION_TEMPLATE.md` | Padrao de decisao tecnica | Ativo | Preencher em mudancas estruturais/sensiveis |
| `frontend/README.md` | Guia da aplicacao frontend | Ativo | Manter alinhado com rotas e estrutura atual |
| `frontend/README_NAVIGATION_CHANGES.md` | Historico de mudancas de navegacao | Legado controlado | Nao usar como documentacao operacional atual |

## Regras de escrita para novos .md

- Cada arquivo deve ter um unico objetivo principal
- Se um conteudo ja existe, referenciar com link em vez de copiar
- Todo novo documento deve ser adicionado nesta tabela
- Quando mudar processo critico (deploy, seguranca, auth, pagamentos), atualizar tambem o `README.md`

## Convencao de manutencao

- `Ativo`: documento operacional e confiavel para execucao
- `Revisar`: documento valido, mas precisa ajuste de alinhamento
- `Legado controlado`: documento util para contexto, nao para execucao cega
