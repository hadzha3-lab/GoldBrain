#!/usr/bin/env bash
set -euo pipefail

APK_PATH="${1:-GoldBrain-debug.apk}"
MAX_BYTES=$((50 * 1024 * 1024))

if [ ! -f "$APK_PATH" ]; then
  echo "::error::APK not found: $APK_PATH"
  exit 1
fi

actual_bytes="$(stat -c%s "$APK_PATH")"
actual_mib="$(awk -v b="$actual_bytes" 'BEGIN { printf "%.2f", b / 1024 / 1024 }')"
max_mib="$(awk -v b="$MAX_BYTES" 'BEGIN { printf "%.0f", b / 1024 / 1024 }')"

echo "Universal APK size: ${actual_mib} MiB (budget: ${max_mib} MiB)"

if [ "$actual_bytes" -gt "$MAX_BYTES" ]; then
  echo "::error::Universal APK exceeded the 50 MiB beta size budget."
  exit 1
fi

echo "APK size budget passed."
