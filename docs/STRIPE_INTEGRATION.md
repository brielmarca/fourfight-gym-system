# Stripe Integration Guide

## Overview

The Four Fight Gym System now supports Stripe subscriptions for recurring monthly memberships. Users can subscribe to plans via Stripe Checkout, and the system automatically manages subscriptions through webhooks.

## Environment Variables

Add these to your backend environment (`.env`, Docker, or cloud provider):

```bash
# Stripe API Keys (get from https://dashboard.stripe.com/apikeys)
STRIPE_SECRET_KEY=sk_test_...          # Secret key (NEVER expose to frontend)
STRIPE_PUBLISHABLE_KEY=pk_test_...     # Publishable key (safe for frontend)
STRIPE_WEBHOOK_SECRET=whsec_...        # Webhook signing secret

# Frontend URLs
STRIPE_FRONTEND_SUCCESS_URL=http://localhost:5173/membership/success
STRIPE_FRONTEND_CANCEL_URL=http://localhost:5173/plans
APP_FRONTEND_URL=http://localhost:5173
```

For production, replace `sk_test_` with `sk_live_`, `pk_test_` with `pk_live_`, and update URLs to your production domain.

## Database Migration

The migration `V11__stripe_integration.sql` adds:

- `users.stripe_customer_id` - Stripe customer reference
- `memberships.stripe_subscription_id` - Stripe subscription ID
- `memberships.stripe_price_id` - Stripe price ID
- `memberships.stripe_checkout_session_id` - Checkout session reference
- `memberships.current_period_start` / `current_period_end` - Billing period
- `memberships.cancel_at_period_end` - Scheduled cancellation flag
- `plans.stripe_price_id` / `plans.stripe_product_id` - Stripe product references
- `payments.stripe_payment_intent_id` / `payments.stripe_invoice_id` - Payment references
- `stripe_webhook_events` table - Webhook event logging

Run with:
```bash
cd backend
mvn flyway:migrate
```

## Webhook Setup

### 1. Configure Webhook in Stripe Dashboard

1. Go to [Stripe Dashboard > Developers > Webhooks](https://dashboard.stripe.com/webhooks)
2. Click "Add endpoint"
3. Enter your endpoint URL: `https://your-domain.com/api/stripe/webhook`
4. Select these events:
   - `checkout.session.completed`
   - `invoice.paid`
   - `invoice.payment_failed`
   - `customer.subscription.deleted`
   - `customer.subscription.updated`
5. Save and copy the **Signing secret** (`whsec_...`)
6. Set `STRIPE_WEBHOOK_SECRET` in your environment

### 2. Local Testing with Stripe CLI

Install the [Stripe CLI](https://docs.stripe.com/stripe-cli):

```bash
# Forward webhooks to your local server
stripe listen --forward-to http://localhost:8080/api/stripe/webhook

# Trigger test events
stripe trigger checkout.session.completed
stripe trigger invoice.paid
stripe trigger invoice.payment_failed
stripe trigger customer.subscription.deleted
```

The CLI will output a webhook signing secret. Use it as `STRIPE_WEBHOOK_SECRET`.

### 3. Testing the Full Flow

1. Start backend: `./scripts/start-local.sh` or `npm run dev:backend`
2. Start frontend: `npm run dev:frontend`
3. Start Stripe CLI forwarding: `stripe listen --forward-to http://localhost:8080/api/stripe/webhook`
4. Navigate to `/plans` and select a plan
5. Click "Pagar com Stripe" - you'll be redirected to Stripe Checkout
6. Use Stripe test card `4242 4242 4242 4242` with any future expiry date and any CVV
7. Complete payment - you'll be redirected to the success page
8. Check the webhook logs in the `stripe_webhook_events` table

## Payment Methods

Stripe Checkout supports these payment methods (configure in [Stripe Dashboard > Settings > Payment methods](https://dashboard.stripe.com/settings/payment_methods)):

- **Cards** (Visa, Mastercard, AMEX) - enabled by default
- **MB WAY** - enable in Stripe Dashboard (Portugal)
- **SEPA Direct Debit** - enable in Stripe Dashboard
- **Apple Pay** - requires HTTPS and domain verification
- **Google Pay** - requires HTTPS and domain verification

## Security

- **Webhook signature verification**: All webhooks are verified using `Stripe-Signature` header
- **No card data stored**: All payment processing happens on Stripe-hosted pages
- **Never store CVV**: Compliant with PCI requirements
- **Idempotent webhooks**: Duplicate webhook events are detected and skipped via `stripe_webhook_events` table
- **Ownership checks**: Membership endpoints verify the authenticated user owns the resource
- **Rate limiting**: Checkout endpoints are rate-limited (20 requests/minute)

## Production Deployment

1. **Switch to live keys**: Replace `sk_test_` with `sk_live_` and `pk_test_` with `pk_live_`
2. **Update webhook URL**: Point to your production domain (`https://your-domain.com/api/stripe/webhook`)
3. **Configure HTTPS**: Stripe requires HTTPS for webhook endpoints
4. **Set up Stripe products/prices**: Create products and recurring prices in Stripe Dashboard, then update `plans.stripe_price_id` for each plan
5. **Test with live mode**: Use Stripe's test mode first, then switch to live mode
6. **Monitor webhook failures**: Check `stripe_webhook_events` table for failed events

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/stripe/checkout` | Authenticated | Create Stripe Checkout session |
| POST | `/api/stripe/webhook` | Public (signature verified) | Receive Stripe webhook events |
| POST | `/api/stripe/subscription/cancel` | Authenticated | Cancel current subscription |
| GET | `/api/stripe/subscription` | Authenticated | Get current subscription details |

## Webhook Event Handling

| Event | Action |
|-------|--------|
| `checkout.session.completed` | Links subscription to membership, activates membership |
| `invoice.paid` | Records payment, updates billing period |
| `invoice.payment_failed` | Records failed payment, logs error |
| `customer.subscription.deleted` | Cancels membership |
| `customer.subscription.updated` | Updates cancel-at-period-end flag |

## Troubleshooting

**Webhook not received:**
- Check Stripe Dashboard > Webhooks > Events for delivery status
- Verify `STRIPE_WEBHOOK_SECRET` is correct
- Check backend logs for signature verification errors

**Membership not activated after payment:**
- Check `stripe_webhook_events` table for event processing status
- Verify `checkout.session.completed` event was received
- Check backend logs for errors during event processing

**Test card declined:**
- Use `4242 4242 4242 4242` for successful payments
- Use `4000 0000 0000 0002` for declined payments
- See [Stripe testing docs](https://docs.stripe.com/testing) for more test cards
