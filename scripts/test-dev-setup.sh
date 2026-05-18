#!/bin/bash

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "Testing development setup..."
echo ""

# Test 1: Check backend health
echo -n "1. Backend health (http://localhost:8080/actuator/health): "
HEALTH=$(curl -s http://localhost:8080/actuator/health 2>/dev/null)
if [[ "$HEALTH" == *"UP"* ]]; then
    echo -e "${GREEN}✓ PASS${NC}"
else
    echo -e "${RED}✗ FAIL${NC}"
fi

# Test 2: Check admin login
echo -n "2. Admin login (admin@gym.com / Admin123!): "
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@gym.com","password":"Admin123!"}' 2>/dev/null)

if [[ "$RESPONSE" == *"accessToken"* ]]; then
    echo -e "${GREEN}✓ PASS${NC}"
else
    echo -e "${RED}✗ FAIL${NC}"
fi

# Test 3: Check frontend
echo -n "3. Frontend running (http://localhost:5173): "
if lsof -i:5173 -t >/dev/null 2>&1; then
    echo -e "${GREEN}✓ PASS${NC}"
else
    echo -e "${RED}✗ FAIL${NC}"
fi

# Test 4: Check Vite proxy
echo -n "4. Vite proxy (/api → backend): "
PROXY_RESPONSE=$(curl -s http://localhost:5173/api/actuator/health 2>/dev/null)
if [[ "$PROXY_RESPONSE" == *"UP"* ]]; then
    echo -e "${GREEN}✓ PASS${NC}"
else
    echo -e "${RED}✗ FAIL${NC}"
fi

echo ""
echo "Setup test complete."
