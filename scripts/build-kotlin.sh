#!/usr/bin/env bash
set -euo pipefail
repo_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if [[ -z "${JAVA_HOME:-}" ]]; then
  for native_jdk in "$HOME"/.cache/hongguotv/toolchain/jdk-17*/Contents/Home; do
    if [[ -x "$native_jdk/bin/java" ]]; then export JAVA_HOME="$native_jdk"; break; fi
  done
fi
if [[ -z "${ANDROID_HOME:-}" && -d "$HOME/.cache/hongguotv/android-sdk" ]]; then
  export ANDROID_HOME="$HOME/.cache/hongguotv/android-sdk"
fi
"$repo_dir/kotlin-tv/gradlew" -p "$repo_dir/kotlin-tv" :app:lintRelease :app:assembleRelease --console=plain
mkdir -p "$repo_dir/outputs"
native_version="${HONGGUOTV_VERSION_NAME:-$(sed -n 's/.*versionName = .*?: "\([^"]*\)".*/\1/p' "$repo_dir/kotlin-tv/app/build.gradle.kts")}"
[[ "$native_version" =~ ^[0-9][0-9A-Za-z.+-]*$ ]] || { echo "Invalid APK version" >&2; exit 1; }
tv_apk="$repo_dir/outputs/hongguotv-tv-$native_version.apk"
mobile_apk="$repo_dir/outputs/hongguotv-mobile-$native_version.apk"
find "$repo_dir/kotlin-tv/app/build/outputs/apk" -name "*tv*.apk" -exec cp {} "$tv_apk" \; 2>/dev/null || true
find "$repo_dir/kotlin-tv/app/build/outputs/apk" -name "*mobile*.apk" -exec cp {} "$mobile_apk" \; 2>/dev/null || true
echo "Built APKs:"
ls -lh "$repo_dir/outputs"/*.apk 2>/dev/null || true
