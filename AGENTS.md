# Four Fight Gym System — Agent Safety Rules

## 1. General Safety

- Work on a feature branch, never directly on `main`.
- Always start with `git fetch`, `git pull --ff-only`, and a clean status check.
- Use rollback tags before risky frontend or deploy-related changes.
- Do not use `git add .`
- Stage only explicit reviewed files.
- Keep commits small and realistic.
- Do not push or merge unless the task explicitly asks for it.

## 2. Frontend Rules

- Do not redesign the site unless explicitly requested.
- Preserve the dark/red **4Four Fight Academy** identity.
- Preserve existing routes/tabs/pages.
- Do not create new tabs/routes unless explicitly requested.
- Do not break auth, checkout, plans, student area, admin area, schedule, or API calls.
- Home hero/main image should not be changed unless explicitly requested.
- When updating images, only replace mismatched assets in existing sections/pages.
- **Jiu Jitsu sections** must use Jiu Jitsu images.
- **Boxe sections** must use boxing images.
- **Kickboxing sections** must use kickboxing images.
- **Gym/gallery/inauguration sections** may use general inauguration/gym photos.
- Do not use external images.
- Verify referenced files exist with Linux/case-sensitive paths.
- Run `npm run build` from `frontend/` before commit.

## 3. Backend Rules

- Do not touch backend unless the task explicitly asks.
- Preserve JWT auth, RBAC, ownership checks, rate limiting, MFA/TOTP, and Stripe webhook security.
- Do not expose protected endpoints.
- Do not trust frontend `userId`.
- Keep **Controller → Service → Repository → Entity** pattern.
- Run relevant Maven tests for backend changes.

## 4. Production / Deployment Rules

- Frontend deploys automatically through **Cloudflare Pages** after GitHub `main` push.
- Backend deploy is separate and must not be done unless explicitly requested.
- Never touch Hostinger, DNS, Cloudflare, VPS, or Supabase unless the task explicitly says so.
- Never change `api.4fourfight.com` DNS without approval.
- Never change MX/email records without approval.

## 5. Secrets / Security

- Never read, print, commit, or expose `.env` values or secrets.
- Never include API keys, tokens, JWT/private keys, cookies, database URLs, Stripe keys, or webhook secrets in output.
- If a command might print secrets, do not run it.

## 6. Validation

- For frontend changes: run `npm run build` from `frontend/`.
- Run `lint`/`typecheck` if scripts exist.
- For backend changes: run targeted Maven tests.
- Show `git status`, `diff --stat`, and changed files before committing.

---

## Project Architecture

| Layer     | Stack                                                           |
| --------- | --------------------------------------------------------------- |
| Frontend  | React + TypeScript / Vite — deployed via Cloudflare Pages       |
| Backend   | Spring Boot / Java — runs on VPS with Docker Compose + Caddy    |
| Database  | Supabase / PostgreSQL                                           |
| Domain    | Hostinger (registration only)                                   |
| DNS / CDN | Cloudflare                                                      |

### Key Configuration

| Variable        | Value                           |
| --------------- | ------------------------------- |
| API URL         | `https://api.4fourfight.com/api` |
| Frontend env    | `VITE_API_URL` (not `VITE_API_BASE_URL`) |
