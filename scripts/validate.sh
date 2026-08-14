#!/bin/bash
set -e

echo "Validating Lounge application..."

for i in {1..10}; do
  if curl -fsS http://localhost:8080/health > /dev/null; then
    echo "Health check passed."
    exit 0
  fi

  echo "Waiting for application... ($i/10)"
  sleep 3
done

echo "Health check failed."
journalctl -u lounge -n 50 --no-pager || true

exit 1