#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT" || exit 1

mkdir -p logs

echo "=== Starting Gym Management System ==="

# Start Backend
echo "[1/2] Starting backend..."
cd backend || exit 1

if lsof -i:8080 -t >/dev/null 2>&1; then
    echo "  Killing existing process on port 8080..."
    lsof -i:8080 -t | xargs kill -9 2>/dev/null
    sleep 2
fi

nohup mvn spring-boot:run -Dspring-boot.run.fork=false > ../logs/backend.log 2>&1 & disown
echo $! > ../logs/backend.pid
echo "  Backend started (PID: $(cat ../logs/backend.pid))"

cd ..

# Start Frontend
echo "[2/2] Starting frontend..."
cd frontend || exit 1

if lsof -i:5173 -t >/dev/null 2>&1; then
    echo "  Killing existing process on port 5173..."
    lsof -i:5173 -t | xargs kill -9 2>/dev/null
    sleep 2
fi

nohup npm run dev > ../logs/frontend.log 2>&1 & disown
echo $! > ../logs/frontend.pid
echo "  Frontend started (PID: $(cat ../logs/frontend.pid))"

cd ..

# Verify
echo ""
echo "=== Verifying ==="
sleep 15

for i in {1..10}; do
    HEALTH=$(curl -s http://localhost:8080/actuator/health 2>/dev/null)
    if [[ "$HEALTH" == *"UP"* ]]; then
        echo "✓ Backend: http://localhost:8080"
        break
    fi
    [ $i -eq 10 ] && echo "✗ Backend failed - check logs/backend.log"
    sleep 2
done

for i in {1..5}; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:5173 2>/dev/null)
    if [[ "$HTTP_CODE" == "200" || "$HTTP_CODE" == "304" ]]; then
        echo "✓ Frontend: http://localhost:5173"
        break
    fi
    [ $i -eq 5 ] && echo "✗ Frontend failed - check logs/frontend.log"
    sleep 2
done

echo ""
echo "Logs: logs/backend.log, logs/frontend.log"
