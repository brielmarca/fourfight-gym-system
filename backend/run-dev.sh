#!/bin/bash
# Run backend and keep it running

cd "$(dirname "$0")"

echo "========================================"
echo "  Starting Gym Management Backend"
echo "========================================"

# Kill any existing process on port 8080
echo "[1/3] Checking port 8080..."
if lsof -i:8080 -t >/dev/null 2>&1; then
    echo "  Port 8080 is in use. Stopping existing process..."
    lsof -i:8080 -t 2>/dev/null | xargs kill -9 2>/dev/null
    sleep 2
fi

# Start backend with nohup
echo "[2/3] Starting Spring Boot application..."
nohup mvn spring-boot:run -Dspring-boot.run.fork=false > backend-dev.log 2>&1 &

BACKEND_PID=$!
echo "  Backend starting with PID: $BACKEND_PID"

# Wait for backend to be ready
echo "[3/3] Waiting for backend to be ready..."
for i in {1..60}; do
    if lsof -i:8080 -t >/dev/null 2>&1; then
        # Verify health endpoint
        HEALTH=$(curl -s http://localhost:8080/actuator/health 2>/dev/null)
        if [[ "$HEALTH" == *"UP"* ]]; then
            echo ""
            echo "========================================"
            echo "  ✓ Backend is running!"
            echo "  - URL: http://localhost:8080"
            echo "  - Health: http://localhost:8080/actuator/health"
            echo "  - Log: backend-dev.log"
            echo "========================================"
            # Keep the script running by waiting for the backend process
            wait $BACKEND_PID
            exit 0
        fi
    fi
    sleep 2
    echo -n "."
done

echo ""
echo "✗ Backend failed to start in time. Check backend-dev.log"
exit 1
