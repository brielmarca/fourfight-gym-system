#!/bin/bash

set -e

echo "Generating RSA 2048-bit key pair for JWT..."

mkdir -p keys

openssl genrsa -out keys/jwt-private.pem 2048
openssl rsa -in keys/jwt-private.pem -pubout -out keys/jwt-public.pem

echo ""
echo "Keys generated:"
echo "  Private key: keys/jwt-private.pem"
echo "  Public key:  keys/jwt-public.pem"
echo ""

echo "Private key ( PEM format - use as JWT_PRIVATE_KEY ):"
cat keys/jwt-private.pem
echo ""

echo "Public key ( PEM format - use as JWT_PUBLIC_KEY ):"
cat keys/jwt-public.pem
echo ""

echo "Now set these in your environment or .env file:"
echo "JWT_PRIVATE_KEY=\"$(cat keys/jwt-private.pem | tr '\n' ' ' | sed 's/ $//')\""
echo "JWT_PUBLIC_KEY=\"$(cat keys/jwt-public.pem | tr '\n' ' ' | sed 's/ $//')\""