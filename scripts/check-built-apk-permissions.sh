#!/usr/bin/env bash
set -euo pipefail

APK_PATH="${1:-GoldBrain-debug.apk}"

if [ ! -f "$APK_PATH" ]; then
  echo "::error::APK not found: $APK_PATH"
  exit 1
fi

AAPT="${ANDROID_HOME:-}/build-tools/35.0.0/aapt"
if [ ! -x "$AAPT" ]; then
  AAPT="$(find "${ANDROID_HOME:-/usr/local/lib/android/sdk}/build-tools" -type f -name aapt | sort -V | tail -n 1)"
fi

if [ -z "${AAPT:-}" ] || [ ! -x "$AAPT" ]; then
  echo "::error::aapt not found; cannot verify final APK permissions."
  exit 1
fi

permissions="$($AAPT dump permissions "$APK_PATH")"
printf '%s\n' "$permissions"

for forbidden in \
  android.permission.INTERNET \
  android.permission.ACCESS_NETWORK_STATE \
  android.permission.CHANGE_NETWORK_STATE \
  android.permission.WRITE_EXTERNAL_STORAGE \
  android.permission.WRITE_MEDIA_STORAGE \
  android.permission.MANAGE_EXTERNAL_STORAGE \
  android.permission.MANAGE_MEDIA; do
  if grep -Fq "$forbidden" <<<"$permissions"; then
    echo "::error::Forbidden permission found in final APK: $forbidden"
    exit 1
  fi
done

echo "Final APK permission check passed: no network or broad media-write permissions."
