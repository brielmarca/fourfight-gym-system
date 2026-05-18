# Four Fight Gym System

A full-stack gym management system for Four Fight Academy - a martial arts academy.

## Structure

```
fourfight-gym-system/
├── backend/          # Spring Boot backend
│   ├── src/main/java/com/gym/
│   │   ├── config/       # Spring configuration
│   │   ├── controller/   # REST API endpoints
│   │   ├── dto/          # Data transfer objects
│   │   ├── entity/       # JPA entities
│   │   ├── security/     # JWT auth, rate limiting
│   │   └── service/      # Business logic
│   └── docker-compose.yml
├── frontend/         # React/Vite frontend
│   └── src/
│       ├── contexts/     # Auth context (global state)
│       ├── queries/      # TanStack Query hooks (API data)
│       ├── providers/    # Query provider
│       ├── types/        # Centralized TypeScript types
│       ├── routes/       # File-based routes
│       ├── components/   # UI components
│       └── lib/          # API client, utilities
├── media/            # Media assets
├── logs/             # Runtime logs
├── docs/             # Documentation
├── scripts/          # Start/stop scripts
├── archive/          # Old/unused items
├── package.json      # Root scripts
└── README.md
```

## Quick Start

### Prerequisites
- Java 21+
- Node.js 18+
- Maven 3.8+
- MySQL 8+

### Start the System

```bash
# From fourfight-gym-system directory
./scripts/start-local.sh
```

Or use npm scripts:

```bash
npm run dev:full    # Start both frontend and backend
npm run dev:frontend  # Start only frontend
npm run dev:backend   # Start only backend
```

### Stop the System

```bash
./scripts/stop-local.sh
```

## Services

- **Backend**: http://localhost:8080
- **Frontend**: http://localhost:5173
- **Health Check**: http://localhost:8080/actuator/health

## Frontend Architecture

The frontend uses a clear state management strategy:

| Layer | Tool | Purpose |
|-------|------|---------|
| Auth State | React Context (`useAuth`) | Logged user, login, logout, roles |
| API Data | TanStack Query | All server data (plans, memberships, classes, etc.) |
| UI State | Local `useState` | Forms, modals, filters, loading states |

See `frontend/README.md` for full details.

## Documentation

See `docs/` directory:
- `PROJECT_CONTEXT.md` - Full project context
- `DEV_SETUP.md` - Development setup guide

## Testing

```bash
./scripts/test-dev-setup.sh
```
