#!/bin/bash
cd /home/brielmarca/Documents/gym-management-backend
java -jar target/gym-management-backend-1.0.0.jar --spring.profiles.active=default > /tmp/backend.log 2>&1 &
echo $! > /tmp/backend.pid
echo "Backend started with PID: $(cat /tmp/backend.pid)"
