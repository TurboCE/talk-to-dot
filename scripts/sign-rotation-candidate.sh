#!/usr/bin/env bash
# Prepare/verify an APK locally. Does not install, upload, or change an existing signer.
set -euo pipefail
umask 077
: "${ANDROID_SDK_ROOT:?Set ANDROID_SDK_ROOT}"
: "${LEGACY_KEYSTORE_PATH:?Set LEGACY_KEYSTORE_PATH}"
: "${RELEASE_KEYSTORE_PATH:?Set RELEASE_KEYSTORE_PATH}"
: "${SIGNING_LINEAGE_PATH:?Set SIGNING_LINEAGE_PATH}"
: "${DOT_LEGACY_STOREPASS:?Set DOT_LEGACY_STOREPASS}"
: "${DOT_RELEASE_STOREPASS:?Set DOT_RELEASE_STOREPASS}"
[[ $# == 2 ]] || { echo 'Usage: sign-rotation-candidate.sh ALIGNED_UNSIGNED_APK OUTPUT_APK' >&2; exit 1; }
if [[ -n "${JAVA_HOME:-}" ]]; then export PATH="$JAVA_HOME/bin:$PATH"; fi
python3 - "$1" "$2" <<'PY'
import os,sys
if os.path.realpath(sys.argv[1])==os.path.realpath(sys.argv[2]):
    raise SystemExit('Use a separate output; input APK must remain unchanged.')
PY
build_tools="$ANDROID_SDK_ROOT/build-tools/${BUILD_TOOLS_VERSION:-36.1.0}"
"$build_tools/zipalign" -c 4 "$1"
"$build_tools/apksigner" sign \
  --ks "$LEGACY_KEYSTORE_PATH" --ks-key-alias "${LEGACY_KEY_ALIAS:-androiddebugkey}" \
  --ks-pass env:DOT_LEGACY_STOREPASS --key-pass env:DOT_LEGACY_STOREPASS \
  --next-signer --ks "$RELEASE_KEYSTORE_PATH" --ks-key-alias "${RELEASE_KEY_ALIAS:-local}" \
  --ks-pass env:DOT_RELEASE_STOREPASS --key-pass env:DOT_RELEASE_STOREPASS \
  --lineage "$SIGNING_LINEAGE_PATH" --rotation-min-sdk-version 33 \
  --debuggable-apk-permitted false --out "$2" "$1"
"$build_tools/apksigner" verify --verbose --print-certs "$2"
echo 'Prepared local rotation candidate only; no device or release was changed.'
