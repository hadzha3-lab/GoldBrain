#!/usr/bin/env bash
set -euo pipefail

APK_PATH="${1:-GoldBrain-debug.apk}"
MAX_MIB="${2:-150}"
LABEL="${3:-APK}"
MAX_BYTES=$((MAX_MIB * 1024 * 1024))

if [ ! -f "$APK_PATH" ]; then
  echo "::error::APK not found: $APK_PATH"
  exit 1
fi

actual_bytes="$(stat -c%s "$APK_PATH")"
actual_mib="$(awk -v b="$actual_bytes" 'BEGIN { printf "%.2f", b / 1024 / 1024 }')"

echo "${LABEL} size: ${actual_mib} MiB (budget: ${MAX_MIB} MiB)"

if [ "$actual_bytes" -gt "$MAX_BYTES" ]; then
  echo "::error::${LABEL} exceeded the ${MAX_MIB} MiB beta size budget."
  exit 1
fi

echo "${LABEL} size budget passed."
