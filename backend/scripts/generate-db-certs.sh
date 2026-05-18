#!/bin/bash
set -e

CERT_DIR="$(dirname "$0")/../certs"
mkdir -p "$CERT_DIR"

echo "Generating PostgreSQL SSL certificate..."

openssl req -new -x509 -days 365 -nodes \
    -text -out "$CERT_DIR/server.crt" \
    -keyout "$CERT_DIR/server.key" \
    -subj "/CN=gym-postgres/O=GymManagement/L=Local/ST=Local/C=US"

chmod 600 "$CERT_DIR/server.key"
chmod 644 "$CERT_DIR/server.crt"

echo "SSL certificates generated:"
echo "  Certificate: $CERT_DIR/server.crt"
echo "  Private key:  $CERT_DIR/server.key"
echo ""
echo "Add to your .env file:"
echo "DB_SSL_CERT=$CERT_DIR/server.crt"