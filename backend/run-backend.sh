#!/bin/bash
cd /home/brielmarca/Documents/gym-management-backend
export DB_USERNAME=gymadmin
export DB_PASSWORD=gymsecretpassword
export JWT_PRIVATE_KEY="$(cat private_key.pem)"
export JWT_PUBLIC_KEY="$(cat public_key.pem)"
export CORS_ALLOWED_ORIGINS="http://localhost:5173,http://localhost:3000,http://localhost:8080"
exec java -jar target/gym-management-backend-1.0.0.jar --spring.profiles.active=dev-h2
