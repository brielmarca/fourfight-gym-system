#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT" || exit 1

echo "=== Stopping Gym Management System ==="

if [ -f logs/backend.pid ]; then
    PID=$(cat logs/backend.pid)
    kill -9 "$PID" 2>/dev/null && echo "✓ Backend stopped" || echo "Backend not running"
    rm -f logs/backend.pid
fi

if [ -f logs/frontend.pid ]; then
    PID=$(cat logs/frontend.pid)
    kill -9 "$PID" 2>/dev/null && echo "✓ Frontend stopped" || echo "Frontend not running"
    rm -f logs/frontend.pid
fi

echo "=== Done ==="
