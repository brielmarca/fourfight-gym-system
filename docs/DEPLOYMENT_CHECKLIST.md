# Four Fight Gym System — Deployment Checklist

## 0. Push to GitHub

After creating the empty repository `fourfight-gym-system` on GitHub, run:

```bash
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/fourfight-gym-system.git
git push -u origin main
```

Replace `YOUR_USERNAME` with your actual GitHub username.

Verify:
```bash
git remote -v
git log --oneline -1
```

---

## 1. Supabase PostgreSQL

### 1.1 Create/Configure Project
- [ ] Create Supabase project at https://supabase.com (or use existing)
- [ ] Note the **Project Ref ID** (e.g., `tqwtvbjiwohustvylkwx`)
- [ ] Go to **Project Settings → Database → Connection string → URI**
- [ ] Copy the connection string (will be used in Render)

### 1.2 Database Setup
- [ ] Go to **SQL Editor** in Supabase dashboard
- [ ] Run the Flyway migrations manually OR let the backend auto-run them on first deploy
- [ ] Migration files are in: `backend/src/main/resources/db/migration/V1__schema.sql` through `V11__stripe_integration.sql`
- [ ] Verify tables exist: `users`, `students`, `plans`, `memberships`, `subscriptions`, `payments`, `attendance`, etc.

### 1.3 Security
- [ ] Set a strong database password (Supabase auto-generates one)
- [ ] Go to **Project Settings → Database → Password** → Reset if needed
- [ ] **Never commit this password** — it goes only in Render env vars

### 1.4 Get Credentials for Render
- [ ] `DATABASE_URL` = `jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres?sslmode=require`
- [ ] `DB_USERNAME` = `postgres.<project-ref>`
- [ ] `DB_PASSWORD` = (from Supabase settings)

---

## 2. Render Backend

### 2.1 Create Service
- [ ] Go to https://dashboard.render.com → **New +** → **Web Service**
- [ ] Connect your GitHub repository `fourfight-gym-system`
- [ ] Configure:
  - **Name**: `fourfight-gym-api`
  - **Root Directory**: `backend`
  - **Build Command**: `./mvnw clean package -DskipTests`
  - **Start Command**: `java -jar target/*.jar`
  - **Runtime**: `Java` (or use Docker if you prefer the Dockerfile)
  - **Region**: `Frankfurt` (closest to Supabase EU)
  - **Plan**: `Free` (or upgrade for production)

### 2.2 Set Environment Variables
In Render dashboard → your service → **Environment**:

| Variable | Value | Notes |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` | Fixed |
| `DATABASE_URL` | `jdbc:postgresql://db.<ref>.supabase.co:5432/postgres?sslmode=require` | From Supabase |
| `DB_USERNAME` | `postgres.<ref>` | From Supabase |
| `DB_PASSWORD` | `<supabase_password>` | **SECRET** |
| `JWT_PRIVATE_KEY` | `<single-line PEM>` | See key generation below |
| `JWT_PUBLIC_KEY` | `<single-line PEM>` | See key generation below |
| `CORS_ALLOWED_ORIGINS` | `https://your-domain.pages.dev` | Cloudflare Pages URL |
| `APP_FRONTEND_URL` | `https://your-domain.pages.dev` | Cloudflare Pages URL |
| `STRIPE_SECRET_KEY` | `sk_live_...` or `sk_test_...` | **SECRET** |
| `STRIPE_PUBLISHABLE_KEY` | `pk_live_...` or `pk_test_...` | Public, safe |
| `STRIPE_WEBHOOK_SECRET` | `whsec_...` | **SECRET** (set after webhook config) |
| `STRIPE_FRONTEND_SUCCESS_URL` | `https://your-domain.pages.dev/membership/success` | |
| `STRIPE_FRONTEND_CANCEL_URL` | `https://your-domain.pages.dev/plans` | |

### 2.3 Generate JWT Keys (RS256)
Run locally (never commit the output):
```bash
# Generate private key
openssl genrsa -out private_key.pem 2048

# Extract public key
openssl rsa -in private_key.pem -pubout -out public_key.pem

# Convert to single-line for Render env var
PRIVATE_KEY_SINGLE=$(awk -v ORS='\\n' '1' private_key.pem)
PUBLIC_KEY_SINGLE=$(awk -v ORS='\\n' '1' public_key.pem)

echo "PRIVATE_KEY_SINGLE:"
echo "$PRIVATE_KEY_SINGLE"
echo ""
echo "PUBLIC_KEY_SINGLE:"
echo "$PUBLIC_KEY_SINGLE"
```

Copy the single-line values into Render environment variables.

### 2.4 Deploy
- [ ] Click **Deploy** in Render
- [ ] Wait for build to complete (~3-5 minutes)
- [ ] Check logs for startup errors
- [ ] Visit `https://<your-service>.onrender.com/actuator/health` — should return `{"status":"UP"}`
- [ ] Note the backend URL: `https://<your-service>.onrender.com`

---

## 3. Cloudflare Pages Frontend

### 3.1 Create Project
- [ ] Go to https://dash.cloudflare.com → **Workers & Pages** → **Create** → **Pages**
- [ ] Connect to GitHub repository `fourfight-gym-system`
- [ ] Configure:
  - **Framework preset**: `React (Vite)`
  - **Root directory**: `frontend`
  - **Build command**: `npm run build`
  - **Build output directory**: `dist`
  - **Environment variables** (see below)

### 3.2 Set Environment Variables
In Cloudflare Pages → your project → **Settings** → **Environment variables**:

| Variable | Value | Notes |
|---|---|---|
| `VITE_API_URL` | `https://<render-service>.onrender.com/api` | Your Render backend URL |
| `VITE_STRIPE_PUBLISHABLE_KEY` | `pk_live_...` or `pk_test_...` | Same as backend |

### 3.3 Deploy
- [ ] Trigger deploy (automatic on push to `main`)
- [ ] Wait for build to complete
- [ ] Note the frontend URL: `https://<your-project>.pages.dev`
- [ ] Update Render `CORS_ALLOWED_ORIGINS` and `APP_FRONTEND_URL` with this URL
- [ ] Redeploy backend after updating CORS

### 3.4 Custom Domain (Optional)
- [ ] Go to Cloudflare Pages → **Custom domains** → **Set up a custom domain**
- [ ] Add your domain (e.g., `fourfightgym.com`)
- [ ] Update Render env vars with the custom domain URL

---

## 4. Stripe Webhook

### 4.1 Create Webhook Endpoint
- [ ] Go to https://dashboard.stripe.com → **Developers** → **Webhooks**
- [ ] Click **Add endpoint**
- [ ] **Endpoint URL**: `https://<render-service>.onrender.com/api/stripe/webhook`
- [ ] **Events to send**:
  - `checkout.session.completed`
  - `customer.subscription.created`
  - `customer.subscription.updated`
  - `customer.subscription.deleted`
  - `invoice.payment_succeeded`
  - `invoice.payment_failed`
- [ ] Click **Add endpoint**

### 4.2 Get Webhook Secret
- [ ] After creating the endpoint, copy the **Signing secret** (`whsec_...`)
- [ ] Add it to Render as `STRIPE_WEBHOOK_SECRET`
- [ ] Redeploy backend

### 4.3 Test Webhook
- [ ] Use Stripe CLI: `stripe listen --forward-to https://<render-service>.onrender.com/api/stripe/webhook`
- [ ] Or use Stripe dashboard → **Send test webhook**
- [ ] Verify backend logs show successful webhook processing

---

## 5. Production Verification

### 5.1 Backend Health
- [ ] `GET https://<render-service>.onrender.com/actuator/health` → `{"status":"UP"}`
- [ ] `GET https://<render-service>.onrender.com/api/.well-known/jwks.json` → returns JWKs
- [ ] `GET https://<render-service>.onrender.com/api/plans` → returns plan list

### 5.2 Frontend
- [ ] Visit `https://<project>.pages.dev` → homepage loads
- [ ] Click **Login** → login form appears
- [ ] Click **Plans** → plans page loads
- [ ] Check browser console — no CORS errors

### 5.3 Authentication Flow
- [ ] Register a new account → success
- [ ] Login → redirects to dashboard
- [ ] Access token works (check network tab)
- [ ] Refresh token rotation works (logout and login again)

### 5.4 Stripe Payment Flow
- [ ] Select a plan → checkout page loads
- [ ] Complete test payment → redirect to success page
- [ ] Verify subscription created in database
- [ ] Verify webhook received and processed

### 5.5 Security Checks
- [ ] No `.env` files in GitHub repo
- [ ] No `.pem`, `.key`, `.crt` files in repo
- [ ] No Stripe secret keys in repo
- [ ] No database passwords in repo
- [ ] CORS only allows your frontend domain
- [ ] JWT keys are RS256 (not HS256)
- [ ] HttpOnly refresh token cookie set
- [ ] Rate limiting active (test rapid requests)

### 5.6 Database
- [ ] Connect to Supabase via SQL editor
- [ ] Verify all tables created by Flyway migrations
- [ ] Verify seed data (roles: `ADMIN`, `MANAGER`, `TRAINER`, `STUDENT`)
- [ ] Test a query: `SELECT * FROM users LIMIT 5;`

---

## Quick Reference URLs

| Service | URL |
|---|---|
| GitHub | `https://github.com/YOUR_USERNAME/fourfight-gym-system` |
| Supabase | `https://supabase.com/dashboard/project/<project-ref>` |
| Render Backend | `https://<service-name>.onrender.com` |
| Cloudflare Pages | `https://<project-name>.pages.dev` |
| Stripe Dashboard | `https://dashboard.stripe.com` |

---

## Troubleshooting

### Backend won't start on Render
- Check logs: Render → your service → **Logs**
- Common issues: wrong `DATABASE_URL`, missing JWT keys, wrong `SPRING_PROFILES_ACTIVE`

### CORS errors in frontend
- Verify `CORS_ALLOWED_ORIGINS` in Render matches your Cloudflare Pages URL exactly
- Must include `https://` prefix
- Redeploy backend after changing

### Stripe webhook 401
- Verify `STRIPE_WEBHOOK_SECRET` matches the signing secret from Stripe dashboard
- Check backend logs for webhook signature errors

### JWT token invalid
- Verify `JWT_PRIVATE_KEY` and `JWT_PUBLIC_KEY` are single-line format with `\n` for newlines
- Keys must match (public key derived from private key)
