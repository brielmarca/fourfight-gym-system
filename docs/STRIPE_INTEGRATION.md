# Stripe Integration Guide

> Status: Preparado (foundation), nao finalizado em producao
> Fonte de verdade do projeto: `README.md`
> Ordem de leitura: `docs/INDEX.md`

## Estado atual

A base tecnica de Stripe ja existe no projeto, mas a ativacao completa em producao ainda depende de:

- dados oficiais da empresa
- definicao final de planos e precos
- configuracao final de conta Stripe e webhook
- validacao end-to-end final em ambiente de producao

Enquanto esses pontos nao forem fechados, trate Stripe como integracao preparada, nao como fluxo comercial plenamente ativo.

## Escopo implementado

- Endpoints backend para checkout/subscription e webhook
- Persistencia de referencias Stripe (customer/subscription/price/checkout/payment)
- Tabela de eventos de webhook para idempotencia e trilha
- Validacao de assinatura de webhook

## Variaveis de ambiente (placeholders)

Use apenas placeholders em documentacao. Nao publicar valores reais.

```bash
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...

STRIPE_FRONTEND_SUCCESS_URL=http://localhost:5173/membership/success
STRIPE_FRONTEND_CANCEL_URL=http://localhost:5173/plans
APP_FRONTEND_URL=http://localhost:5173
```

Para go-live, trocar para chaves live e URLs de producao aprovadas.

## Banco de dados

A migracao `V11__stripe_integration.sql` adiciona campos e tabelas para:

- referencias Stripe em `users`, `memberships`, `plans`, `payments`
- periodo de cobranca da assinatura
- tabela `stripe_webhook_events` para controle de eventos

## Antes da ativacao (go-live checklist)

1. Confirmar produtos/precos finais no Stripe e no banco
2. Configurar endpoint de webhook no dominio oficial da API
3. Confirmar `STRIPE_WEBHOOK_SECRET` no ambiente de backend
4. Validar fluxo completo em modo teste (checkout + webhook + atualizacao de membership)
5. Revalidar em modo live com monitoramento de falhas

## Eventos esperados de webhook

- `checkout.session.completed`
- `invoice.paid`
- `invoice.payment_failed`
- `customer.subscription.deleted`
- `customer.subscription.updated`

## Seguranca

- Nunca expor `STRIPE_SECRET_KEY` ou `STRIPE_WEBHOOK_SECRET` no frontend
- Nunca armazenar dados sensiveis de cartao (CVV, PAN)
- Manter validacao de assinatura do webhook ativa
- Manter ownership checks em endpoints de assinatura/membership

## Observacoes de integracao frontend

- O frontend deve continuar usando `VITE_API_URL` como variavel oficial de API.
- Nao usar `VITE_API_BASE_URL`.

## Referencias operacionais

- Deploy/infra: `docs/DEPLOYMENT.md`
- Checklist de release: `docs/DEPLOYMENT_CHECKLIST.md`
- Contexto geral: `README.md`
