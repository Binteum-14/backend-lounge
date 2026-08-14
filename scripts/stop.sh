#!/bin/bash
set -e

echo "Stopping Lounge application..."

if systemctl is-active --quiet lounge; then
  systemctl stop lounge
fi

rm -f /home/ec2-user/app/app.jar

echo "Lounge application stopped."