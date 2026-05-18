#!/bin/bash
# Firewall setup script for Four Fight Gym System
# Run with sudo: sudo ./setup-firewall.sh

set -e

echo "=== Setting up UFW Firewall ==="

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo "Please run as root (use sudo)"
    exit 1
fi

# Reset UFW to defaults
echo "Resetting UFW..."
ufw --force reset

# Set default policies
echo "Setting default policies..."
ufw default deny incoming
ufw default allow outgoing

# Allow SSH (keep current connection)
echo "Allowing SSH..."
ufw allow 22/tcp comment 'SSH'

# Allow HTTP and HTTPS
echo "Allowing HTTP/HTTPS..."
ufw allow 80/tcp comment 'HTTP'
ufw allow 443/tcp comment 'HTTPS'

# Deny direct access to backend and database ports
echo "Blocking backend/database ports..."
ufw deny 8080/tcp comment 'Block direct backend access'
ufw deny 5432/tcp comment 'Block PostgreSQL external access'
ufw deny 6379/tcp comment 'Block Redis external access'

# Enable UFW
echo "Enabling UFW..."
ufw --force enable

# Show status
echo ""
echo "=== Firewall Status ==="
ufw status verbose

echo ""
echo "✅ Firewall configured successfully!"
echo "   - Only ports 22, 80, 443 are open"
echo "   - Ports 8080, 5432, 6379 are blocked externally"
