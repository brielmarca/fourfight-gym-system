# Development Setup

## Quick Start

### Option 1: Using npm (Recommended)
```bash
# First time setup
npm install

# Start both frontend and backend
npm run dev:full
```

### Option 2: Using bash script
```bash
./start.sh
```

## What This Does

1. **Backend (Spring Boot)**: Runs on `http://localhost:8080`
   - Uses H2 in-memory database by default
   - JWT keys are auto-generated if not configured
   
2. **Frontend (Vite + React)**: Runs on `http://localhost:5173`
   - Proxies `/api` requests to backend automatically
   - Hot reload enabled

## Available Commands

| Command | Description |
|---------|-------------|
| `npm run dev:full` | Start both frontend and backend |
| `npm run dev:frontend` | Start only frontend |
| `npm run dev:backend` | Start only backend |
| `npm run install:all` | Install frontend dependencies |

## Troubleshooting

### Backend won't start
- Ensure Java 21+ is installed: `java -version`
- Ensure Maven is installed: `mvn -version`

### Frontend won't start
- Ensure Node.js is installed: `node -version`
- Delete `node_modules` and run `npm install` in `forge-instinct-site-main`

### Port already in use
- Backend (8080): The start.sh script handles this automatically
- Frontend (5173): Vite will use next available port

## Vite Proxy Configuration

The frontend proxies API requests to the backend:
- `/api/*` → `http://localhost:8080/api/*`

This is configured in `forge-instinct-site-main/vite.config.ts`
