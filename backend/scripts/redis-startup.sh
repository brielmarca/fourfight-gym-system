#!/bin/sh
# Redis startup script that loads ACL

echo "Loading ACL rules..."
redis-cli ACL LOAD || true

echo "Starting Redis server..."
exec redis-server \
    --requirepass "${REDIS_PASSWORD}" \
    --maxmemory 128mb \
    --maxmemory-policy allkeys-lru \
    --protected-mode yes \
    --bind 127.0.0.1 \
    --loglevel notice \
    --save 900 1 \
    --save 300 10 \
    --save 60 10000 \
    --appendonly yes \
    --appendfsync everysec