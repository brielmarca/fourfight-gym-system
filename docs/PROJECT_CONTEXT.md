# Project Context

## Overview

This workspace contains a Four Fight Academy / gym management system split across separate frontend and backend folders.

- `forge-instinct-site-main/`: active React/Vite frontend (SaaS subscription system).
- `gym-management-backend/`: active Java Spring Boot backend API (production-grade).
- `fourfight-academy/`: high-level product and architecture README. It describes the intended system, but the active code is in the two folders above.
- `gymluta/`: media files (photos and videos) for the academy.
- `logs/`: runtime logs.

The product is a martial arts academy platform for public site content, plan selection, registration, memberships, checkout/payment flow, student area, scheduling, and admin/management operations.

## Frontend

Path: `forge-instinct-site-main/`

### Stack:

- React 19
- TypeScript 5.8.3
- Vite 7.3.1 with @lovable.dev/vite-tanstack-config
- TanStack Router with file-based routes
- TanStack Query 5.83.0 (React Query)
- Tailwind CSS 4.2.1
- shadcn/Radix UI components
- lucide-react icons
- React Hook Form 7.71.2 + Zod 3.24.2 (form validation)
- Recharts (charts)
- Framer Motion (animations)
- Embla Carousel (carousel)
- Cloudflare edge support (vite-plugin-cloudflare)

### Project Structure:

```
src/
├── routes/              # File-based app routes (TanStack Router)
│   ├── checkout/       # Checkout flow ($planId.tsx)
│   ├── membership/     # Payment processing and confirmation
│   │   ├── $membershipId.tsx
│   │   ├── $membershipId/form.tsx
│   │   └── success.tsx
│   ├── __root.tsx      # Main layout with SEO meta tags
│   ├── index.tsx       # Home page
│   ├── about.tsx       # About the academy
│   ├── plans.tsx       # Membership plans
│   ├── programs.tsx    # Training programs
│   ├── programas.jiu-jitsu.tsx  # Jiu-Jitsu program details
│   ├── schedule.tsx    # Class schedules
│   ├── login.tsx       # Login
│   ├── register.tsx    # Registration
│   ├── student-area.tsx # Student area
│   ├── admin.tsx       # Admin area
│   ├── contact.tsx     # Contact
│   └── ...
├── components/
│   ├── site/          # Public marketing/site sections
│   │   ├── Navbar.tsx
│   │   ├── Hero.tsx
│   │   ├── Programs.tsx
│   │   ├── Pricing.tsx
│   │   ├── Schedule.tsx
│   │   ├── Academy.tsx
│   │   ├── Contact.tsx
│   │   ├── Footer.tsx
│   │   └── ProgramModal.tsx
│   └── ui/            # Reusable shadcn/Radix-style UI primitives
│       ├── button.tsx, card.tsx, dialog.tsx, form.tsx, etc.
├── lib/               # Utilities and API client
│   └── api.ts         # Frontend API client and auth token handling
├── hooks/             # Custom React hooks
├── router.tsx         # TanStack Router setup
├── routeTree.gen.ts   # Generated route tree
└── styles.css         # Global styles
```

### Important files and folders:

- `src/routes/`: file-based app routes.
- `src/routes/index.tsx`: home page route.
- `src/routes/login.tsx`, `register.tsx`, `student-area.tsx`, `admin.tsx`: auth and protected app areas.
- `src/routes/plans.tsx`, `checkout/$planId.tsx`, `membership/$membershipId.tsx`, `membership/$membershipId/form.tsx`, `membership/success.tsx`: membership and checkout flow.
- `src/components/site/`: public marketing/site sections such as hero, programs, pricing, schedule, contact, footer.
- `src/components/ui/`: reusable shadcn/Radix-style UI primitives.
- `src/lib/api.ts`: frontend API client and auth token handling.
- `src/router.tsx` and `src/routeTree.gen.ts`: TanStack Router setup/generated route tree.
- `src/styles.css`: global styles.
- `vite.config.ts`: Vite config with proxy to backend (localhost:8080).

### Frontend Behavior:

The project works without backend (demo mode) - if the API is unavailable, it generates test IDs and simulates payments with test card (4242 4242 4242 4242).

### Frontend API base URL:

- `VITE_API_URL`, defaulting to `http://localhost:8080/api`.

### Frontend commands:

```bash
cd forge-instinct-site-main
npm install
npm run dev
npm run build
npm run lint
npm run format
```

### Frontend helper script:

```bash
./start-frontend.sh
```

Starts Vite with `--host` and writes logs/PID to `/tmp/frontend.log` and `/tmp/frontend.pid`.

## Backend

Path: `gym-management-backend/`

### Stack:

- Java 21 (with preview features enabled)
- Spring Boot 3.3.0
- Spring Web MVC
- Spring Security 6 with JWT (jjwt 0.12.5, nimbus-jose-jwt)
- Spring Data JPA / Hibernate 6
- PostgreSQL 16 for production/dev profile
- H2 for default and dev-h2 style local runs
- Flyway migrations
- Redis 7 for cache/rate limiting/session-related infrastructure
- MapStruct 1.5.5
- Lombok
- Bucket4j rate limiting (with Redis support)
- springdoc OpenAPI 2.5.0 / Swagger
- TOTP MFA (dev.samstevens.totp)
- Spring Boot Actuator (monitoring)
- Spring Boot Validation (Bean Validation)
- Caffeine cache (local cache)
- Testcontainers dependencies are present, but no `src/test` directory currently exists.

### Backend package root:

- `src/main/java/com/gym/`

### Main backend folders:

```
src/main/java/com/gym/
├── GymManagementApplication.java  # Spring Boot entry point
├── controller/                    # REST controllers
│   ├── AuthController.java
│   ├── UserController.java
│   ├── PlanController.java
│   ├── MembershipController.java
│   ├── ClassController.java
│   ├── TrainerController.java
│   ├── StudentProfileController.java
│   ├── PaymentController.java
│   ├── CheckoutController.java
│   ├── ScheduleController.java
│   ├── BeltController.java
│   ├── ProgramController.java
│   ├── NotificationController.java
│   ├── AttendanceController.java
│   ├── TrialBookingController.java
│   ├── MfaController.java
│   ├── AdminController.java
│   ├── ManagerController.java
│   ├── ContactController.java
│   └── JwksController.java
├── service/                      # Business logic
│   ├── AuthService.java, CheckoutService.java, ClassService.java
│   ├── MembershipService.java, PaymentService.java, PlanService.java
│   ├── TrainerService.java, StudentProfileService.java
│   ├── NotificationService.java, MfaService.java
│   ├── BeltService.java, GraduationService.java
│   ├── TrialBookingService.java, ScheduleRequestService.java
│   ├── StudentMartialArtService.java, MartialArtService.java
│   ├── AuditService.java, RateLimitService.java
│   └── AdminService.java
├── repository/                   # Spring Data repositories
├── entity/                       # JPA entities
│   ├── User.java, Student.java, Trainer.java
│   ├── StudentProfile.java, Belt.java, Graduation.java
│   ├── MartialArt.java, StudentMartialArt.java
│   ├── Plan.java, Membership.java, Subscription.java
│   ├── GymClass.java, ClassEnrollment.java
│   ├── Payment.java, RefreshToken.java
│   ├── TrialBooking.java, Contact.java
│   ├── Notification.java, AuditLog.java
│   └── RateLimitBucket.java
├── dto/                         # Data Transfer Objects
│   ├── request/                 # Input DTOs
│   └── response/               # Output DTOs
├── mapper/                      # MapStruct mappers
├── security/                    # JWT, security config, auth filter, rate limit filter
│   ├── SecurityConfig.java
│   ├── JwtUtil.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   ├── GymUserDetailsService.java
│   └── RateLimitFilter.java
├── config/                      # CORS, Swagger, cache, data initialization
├── exception/                   # Typed exceptions and global exception handling
└── event/                      # Audit events/listeners
```

### Backend domain areas include:

- Auth and MFA (TOTP)
- Users and roles (ADMIN, MANAGER, TRAINER, CLIENT)
- Students and student profiles
- Martial arts and belts (with graduation system)
- Plans and memberships (with subscriptions)
- Checkout and payments
- Classes, attendance, enrollments, schedule requests, trial bookings
- Trainers
- Contacts and notifications
- Admin/manager dashboards and audit logs (with JSON diff)

### Important backend files:

- `pom.xml`: Maven config and dependencies.
- `src/main/java/com/gym/GymManagementApplication.java`: Spring Boot entry point.
- `src/main/resources/application.yml`: default local config using H2.
- `src/main/resources/application-dev.yml`: dev profile using PostgreSQL.
- `src/main/resources/application-dev-h2.yml`: local H2 dev profile if present/used by scripts.
- `src/main/resources/db/migration/`: Flyway SQL migrations (V1__schema.sql to V10).
- `docker-compose.yml`: production-style backend stack with app, nginx, postgres, and redis.
- `Dockerfile`: multi-stage build with Eclipse Temurin 21.
- `nginx.conf`: reverse proxy configuration.
- `postgresql.conf`, `pg_hba.conf`, `redis.acl`: deployment/runtime config.
- `private_key.pem`, `public_key.pem`: local JWT keys.
- `.env`: environment variables (credentials, JWT secrets, CORS).

### Backend commands:

```bash
cd gym-management-backend
./mvnw spring-boot:run
mvn spring-boot:run
mvn test
mvn package
```

This repo does not currently include a Maven wrapper, so use system `mvn` unless one is added later.

### Existing helper scripts:

```bash
./start.sh
./run-backend.sh
```

- `start.sh` runs the packaged jar with the default profile and writes PID/logs to `/tmp/backend.pid` and `/tmp/backend.log`.
- `run-backend.sh` exports local DB/JWT/CORS variables and runs the jar with `--spring.profiles.active=dev-h2`.

### Docker:

```bash
cd gym-management-backend
docker compose up -d --build
```

The Docker stack expects environment variables such as `DB_USERNAME`, `DB_PASSWORD`, `REDIS_PASSWORD`, `REDIS_APP_USER`, `REDIS_APP_PASSWORD`, `REDIS_MONITOR_PASSWORD`, `JWT_PRIVATE_KEY`, `JWT_PUBLIC_KEY`, and `CORS_ALLOWED_ORIGINS`.

## API Shape

The frontend expects backend endpoints under:

```text
http://localhost:8080/api
```

Known controller areas include:

- `/api/auth/*` - Authentication (register, login, refresh token, MFA)
- `/api/plans/*` - Membership plans
- `/api/classes/*` - Classes
- `/api/users/*` - User management
- `/api/admin/*` - Admin dashboard
- `/api/belts/*` - Belt/graduation system
- `/api/student-profile/*` - Student profiles
- `/api/attendance/*` - Attendance tracking
- `/api/memberships/*` - Memberships
- `/api/checkout/*` - Checkout flow
- `/api/payments/*` - Payments
- `/api/trainers/*` - Trainer management
- `/api/contact/*` - Contact/leads
- `/api/notifications/*` - Notifications
- `/api/schedule/*` - Class schedules
- `/api/schedule-requests/*` - Schedule requests
- `/api/trial-bookings/*` - Trial bookings
- `/api/mfa/*` - MFA/TOTP setup and verification
- `/api/jwks/*` - JWKS endpoint

Check the controller classes for exact route names before changing API calls.

## Auth

The frontend stores `accessToken` and `refreshToken` in `localStorage`.

`src/lib/api.ts`:

- Adds `Authorization: Bearer <token>` when an access token exists.
- Refreshes tokens on HTTP 401 by posting to `/auth/refresh`.
- Clears tokens and redirects to `/login` when refresh fails.
- Decodes JWT payload locally for current user and role checks.

Backend auth design from code/readme:

- JWT access token expiration defaults to 15 minutes.
- Refresh token expiration defaults to 7 days.
- Refresh tokens are stored as hashes.
- Roles include `ADMIN`, `MANAGER`, `TRAINER`, and `CLIENT`.
- MFA/TOTP support exists under `MfaController` and `MfaService`.

## Database

Migrations are in:

```text
gym-management-backend/src/main/resources/db/migration/
```

Current migrations include schema, indexes, seed roles, belts/profile/attendance, trial bookings, plan fields/features, student martial arts, subscriptions, and refresh token hash length fixes.

### Default profile:

- Uses in-memory H2.
- Disables Flyway.
- Uses `ddl-auto: update`.

### Dev profile:

- Uses PostgreSQL at `jdbc:postgresql://127.0.0.1:5432/gymdb`.
- Enables Flyway.
- Uses `ddl-auto: validate`.

## Media Files (gymluta)

Path: `gymluta/`

This directory contains media files (photos and videos) for the Gym Luta academy:

- `C0003.mp4` - Promotional/demonstration video (~108MB).
- `DSC06273.jpg` to `DSC06321.jpg` - High-quality photos of the academy space, equipment, or classes (~15-38MB each).
- `Sabor inauguração/` - Folder with 20 photos from the academy inauguration event.

Total estimated size: ~460MB of media files.

This is not a software project but a media assets folder for use in the website or marketing materials.

## Project Documentation (fourfight-academy)

Path: `fourfight-academy/`

This directory contains ONLY the `README.md` file - a detailed specification document for the intended system. No code has been implemented here.

The README describes a complete martial arts academy management platform with:

### Planned Features:

**Member Area:**
- Registration and authentication
- Class schedule navigation
- Class enrollment
- Membership purchase
- Personal dashboard

**Admin Area:**
- User management (CRUD)
- Class schedule management
- Membership plan management
- Trainer management
- Payment registration
- Analytics dashboard
- Contact message management
- Audit logging

**Roles:** ADMIN, MANAGER, TRAINER, CLIENT with different access levels.

### Planned Architecture:

- Controllers → Services → Repositories → Entities
- JWT with access token (15 min) and refresh token (7 days)
- Rate limiting and input validation
- Soft delete on all entities
- Flyway for DB migrations

### Planned Tech Stack:

**Frontend:** React 18 + TypeScript, Next.js 14, Tailwind CSS, Shadcn UI
**Backend:** Java 21 + Spring Boot 3.3, Spring Security 6 + JWT, Spring Data JPA + Hibernate 6
**Infrastructure:** PostgreSQL 16, Redis, Docker + Docker Compose

Note: The active implementation of this spec is in `forge-instinct-site-main/` (frontend) and `gym-management-backend/` (backend). Avoid relying on `fourfight-academy/README.md` paths literally; it describes a planned monorepo layout that is not the current active filesystem layout.

## Development Notes

- Prefer updating API contracts in DTOs and frontend types together.
- For backend features, follow the existing layer pattern: controller -> service -> repository -> entity.
- Put business rules in services, not controllers.
- Use `@Transactional` on service methods that mutate state or need consistency.
- Use MapStruct mappers when converting entities to response DTOs if a mapper already exists for that domain.
- Keep frontend API calls centralized in `src/lib/api.ts`.
- For new frontend routes, follow the existing TanStack Router file route pattern.
- For UI, prefer the existing `src/components/ui` primitives and lucide icons.
- Avoid relying on `fourfight-academy/README.md` paths literally; it describes a planned monorepo layout that is not the current active filesystem layout.

## Verification

Useful checks after changes:

```bash
cd forge-instinct-site-main
npm run lint
npm run build
```

```bash
cd gym-management-backend
mvn test
mvn package
```

No backend tests are currently present under `src/test`, so `mvn test` may only verify compilation/test lifecycle unless tests are added.

## Common Local URLs

- Frontend dev server: `http://localhost:5173`
- Backend API: `http://localhost:8080/api`
- Backend health: `http://localhost:8080/actuator/health`
- Swagger/OpenAPI UI: `http://localhost:8080/swagger-ui.html` or `http://localhost:8080/swagger-ui/index.html`
