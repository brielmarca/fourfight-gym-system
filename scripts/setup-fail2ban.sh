#!/bin/bash
# Fail2Ban installation and setup script for Four Fight Gym System
# Run with sudo: sudo ./setup-fail2ban.sh

set -e

echo "=== Installing and Configuring Fail2Ban ==="

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo "Please run as root (use sudo)"
    exit 1
fi

# Install Fail2Ban
echo "Installing Fail2Ban..."
apt-get update
apt-get install -y fail2ban

# Copy configuration
echo "Copying Fail2Ban configuration..."
cp "$(dirname "$0")/../backend/fail2ban/jail.local" /etc/fail2ban/jail.local

# Restart Fail2Ban
echo "Restarting Fail2Ban service..."
systemctl restart fail2ban
systemctl enable fail2ban

# Show status
echo ""
echo "=== Fail2Ban Status ==="
fail2ban-client status
fail2ban-client status sshd
fail2ban-client status nginx-limit-req

echo ""
echo "✅ Fail2Ban configured successfully!"
echo "   - SSH brute force protection enabled"
echo "   - Nginx rate limit violations monitored"
echo "   - Ban time: 1 hour (SSH: 24 hours)"
echo ""
echo "Monitor bans with: sudo fail2ban-client status"
