#!/usr/bin/env bash
set -euo pipefail

MANIFEST="app/src/main/AndroidManifest.xml"
SOURCE_ROOT="app/src/main/java"
BUILD_FILE="app/build.gradle.kts"
VERSION_CATALOG="gradle/libs.versions.toml"

fail() {
  echo "::error::$1"
  exit 1
}

if grep -n -E 'android\.permission\.(INTERNET|ACCESS_NETWORK_STATE|CHANGE_NETWORK_STATE)' "$MANIFEST"; then
  fail "GoldBrain beta must remain local-only and must not request network permissions."
fi

if grep -n 'com.google.mlkit.vision.DEPENDENCIES' "$MANIFEST"; then
  fail "GoldBrain beta must bundle ML models instead of requesting Play Services model downloads."
fi

if grep -RIn --exclude-dir=build -E \
  'java\.net\.(URL|URI|HttpURLConnection|HttpsURLConnection)|okhttp3|retrofit2|io\.ktor\.client|com\.android\.volley|android\.webkit\.WebView' \
  "$SOURCE_ROOT"; then
  fail "GoldBrain beta must not contain direct network or remote WebView code."
fi

if grep -n -E \
  'okhttp|retrofit|ktor-client|volley|firebase-(storage|database|firestore|functions)' \
  "$BUILD_FILE"; then
  fail "GoldBrain beta must not include network/cloud client dependencies."
fi

if grep -n -E \
  'com\.google\.android\.gms:play-services-mlkit-(text-recognition|image-labeling)' \
  "$VERSION_CATALOG"; then
  fail "GoldBrain beta must use bundled ML Kit OCR/image-labeling models."
fi

echo "Offline beta check passed: runtime has no direct network capability and core ML models are bundled."
