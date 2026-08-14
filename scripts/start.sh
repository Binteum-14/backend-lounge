#!/bin/bash
set -e

echo "Starting Lounge application..."

systemctl daemon-reload
systemctl start lounge

echo "Lounge application started."