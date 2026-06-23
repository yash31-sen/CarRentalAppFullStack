#!/usr/bin/env bash
# =============================================================================
# ec2-setup.sh — One-time server setup for a fresh Ubuntu 22.04 EC2 instance
#
# Run this ONCE after launching your EC2:
#   chmod +x ec2-setup.sh && ./ec2-setup.sh
# =============================================================================
set -euo pipefail

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  EC2 Setup: Docker + Docker Compose + AWS CLI"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# ── 1. Update packages ────────────────────────────────────────────────────────
echo ""
echo "▶ Updating apt packages..."
sudo apt-get update -y
sudo apt-get upgrade -y

# ── 2. Install Docker ─────────────────────────────────────────────────────────
echo ""
echo "▶ Installing Docker..."
sudo apt-get install -y ca-certificates curl gnupg lsb-release

# Add Docker's official GPG key
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# Add Docker repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io \
  docker-buildx-plugin docker-compose-plugin

# Allow current user to run docker without sudo
sudo usermod -aG docker "$USER"
echo "  ✅ Docker installed"

# ── 3. Install AWS CLI v2 ─────────────────────────────────────────────────────
echo ""
echo "▶ Installing AWS CLI v2..."
sudo apt-get install -y unzip
curl -fsSL "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o /tmp/awscliv2.zip
unzip -q /tmp/awscliv2.zip -d /tmp
sudo /tmp/aws/install
rm -rf /tmp/awscliv2.zip /tmp/aws
echo "  ✅ AWS CLI installed: $(aws --version)"

# ── 4. Configure AWS CLI on EC2 (uses IAM Role or manual keys) ───────────────
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  AWS CLI Configuration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "  RECOMMENDED: Attach an IAM Role with ECR pull permissions to this EC2"
echo "  instance via the AWS Console (EC2 → Actions → Security → Modify IAM Role)."
echo "  Policy needed:  AmazonEC2ContainerRegistryReadOnly"
echo ""
echo "  OR manually configure credentials (less secure):"
echo "    aws configure"
echo ""

# ── 5. Verify installations ───────────────────────────────────────────────────
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Installation Summary"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
docker --version
docker compose version
aws --version

echo ""
echo "✅ Setup complete!"
echo ""
echo "⚠  IMPORTANT: Log out and back in (or run 'newgrp docker') so Docker"
echo "   commands work without sudo."
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  EC2 Security Group — Required inbound rules:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Port 22   (SSH)   — your IP only"
echo "  Port 80   (HTTP)  — 0.0.0.0/0  (public web traffic)"
echo "  Port 8080 is internal only — do NOT open it publicly"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
