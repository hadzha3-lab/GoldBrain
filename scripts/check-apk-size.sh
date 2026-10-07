#!/usr/bin/env bash
set -euo pipefail

APK_PATH="${1:-GoldBrain-debug.apk}"
# GoldBrain 0.26+ intentionally bundles its ML models so first-run analysis works
# without downloading model payloads. Keep a guard against accidental bloat, but
# allow the expected ~127 MiB universal debug APK.
MAX_BYTES=$((150 * 1024 * 1024))

if [ ! -f "$APK_PATH" ]; then
  echo "::error::APK not found: $APK_PATH"
  exit 1
fi

actual_bytes="$(stat -c%s "$APK_PATH")"
actual_mib="$(awk -v b="$actual_bytes" 'BEGIN { printf "%.2f", b / 1024 / 1024 }')"
max_mib="$(awk -v b="$MAX_BYTES" 'BEGIN { printf "%.0f", b / 1024 / 1024 }')"

echo "Universal APK size: ${actual_mib} MiB (budget: ${max_mib} MiB)"

if [ "$actual_bytes" -gt "$MAX_BYTES" ]; then
  echo "::error::Universal APK exceeded the ${max_mib} MiB beta size budget."
  exit 1
fi

echo "APK size budget passed."
