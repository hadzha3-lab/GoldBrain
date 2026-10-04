#!/usr/bin/env bash
set -euo pipefail

ROOT="app/src/main"

fail() {
  echo "::error::$1"
  exit 1
}

if grep -RIn --exclude-dir=build -E   'WRITE_EXTERNAL_STORAGE|WRITE_MEDIA_STORAGE|MANAGE_EXTERNAL_STORAGE|MANAGE_MEDIA'   "$ROOT"; then
  fail "GoldBrain must never request broad media write/manage permissions."
fi

if grep -RIn --exclude-dir=build -E   'FLAG_GRANT_WRITE_URI_PERMISSION|MODE_WRITE_ONLY|MODE_READ_WRITE|MODE_CREATE|MODE_TRUNCATE'   "$ROOT"; then
  fail "GoldBrain must never request writable access to user media URIs."
fi

if grep -RIn --exclude-dir=build -E   'contentResolver[[:space:]]*\.[[:space:]]*(delete|update)[[:space:]]*\('   "$ROOT"; then
  fail "GoldBrain must never delete or update content through ContentResolver."
fi

if grep -RIn --exclude-dir=build -E   '(createDeleteRequest|createWriteRequest|createTrashRequest|createFavoriteRequest)[[:space:]]*\('   "$ROOT"; then
  fail "GoldBrain must never request MediaStore mutation access."
fi

if grep -RIn --exclude-dir=build -E   'DocumentsContract[[:space:]]*\.[[:space:]]*deleteDocument|Intent[[:space:]]*\.[[:space:]]*ACTION_DELETE'   "$ROOT"; then
  fail "GoldBrain must never invoke document/media deletion APIs."
fi

if grep -RIn --exclude-dir=build -E   'contentResolver[[:space:]]*\.[[:space:]]*openOutputStream[[:space:]]*\('   "$ROOT"; then
  fail "GoldBrain must never open user media output streams."
fi

if grep -RIn --exclude-dir=build -E   'openFileDescriptor[[:space:]]*\([^,]+,[[:space:]]*"(w|wa|rw|rwt)"'   "$ROOT"; then
  fail "GoldBrain must never open user media file descriptors in write mode."
fi

if grep -RIn --exclude-dir=build   --exclude=LocalStorageManager.kt -E   '\.delete[[:space:]]*\([[:space:]]*\)|\.deleteRecursively[[:space:]]*\(|Files[[:space:]]*\.[[:space:]]*delete'   "$ROOT"; then
  fail "File deletion is forbidden outside explicitly allowlisted app-owned storage code."
fi

echo "Media safety check passed: existing gallery media is read-only."
