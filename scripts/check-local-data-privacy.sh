#!/usr/bin/env bash
set -euo pipefail

MANIFEST="app/src/main/AndroidManifest.xml"
LEGACY_RULES="app/src/main/res/xml/backup_rules.xml"
MODERN_RULES="app/src/main/res/xml/data_extraction_rules.xml"

fail() {
  echo "::error::$1"
  exit 1
}

grep -q 'android:allowBackup="false"' "$MANIFEST" ||
  fail "GoldBrain local index must not opt into Android cloud backup."

grep -q 'android:fullBackupContent="@xml/backup_rules"' "$MANIFEST" ||
  fail "Legacy backup exclusion rules are missing."

grep -q 'android:dataExtractionRules="@xml/data_extraction_rules"' "$MANIFEST" ||
  fail "Android 12+ data extraction rules are missing."

for domain in root file database sharedpref external; do
  grep -q "domain=\"$domain\"" "$LEGACY_RULES" ||
    fail "Legacy backup rules do not exclude $domain."

  count="$(
    grep -c "domain=\"$domain\"" "$MODERN_RULES" || true
  )"

  if [ "$count" -lt 2 ]; then
    fail "Android 12+ rules must exclude $domain from both cloud and device transfer."
  fi
done

echo "Local-data privacy check passed: GoldBrain index is excluded from backup and transfer."
