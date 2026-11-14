#!/usr/bin/env bash
# Builds StatusClock.apk with the plain Android SDK tools (no Gradle).
# Needs ANDROID_HOME with platforms;android-34 and build-tools;35.0.0.
set -euo pipefail
cd "$(dirname "$0")"
SDK="${ANDROID_HOME:-/opt/android-sdk}"
BT="$SDK/build-tools/35.0.0"
JAR="$SDK/platforms/android-34/android.jar"
rm -rf build && mkdir -p build/classes build/dex

"$BT/aapt2" compile --dir res -o build/res.zip
"$BT/aapt2" link -I "$JAR" --manifest AndroidManifest.xml -o build/unsigned.apk \
    --java build/gen build/res.zip
javac --release 8 -classpath "$JAR" -d build/classes \
    $(find src build/gen -name '*.java') 2>&1 | grep -v 'JAVA_TOOL_OPTIONS' || true
"$BT/d8" --min-api 26 --lib "$JAR" --output build/dex $(find build/classes -name '*.class')
(cd build/dex && zip -q ../unsigned.apk classes.dex)
"$BT/zipalign" -f 4 build/unsigned.apk build/aligned.apk

KS=build-keystore.jks
if [ ! -f "$KS" ]; then
  keytool -genkeypair -keystore "$KS" -storepass android -keypass android -alias clock \
      -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=StatusClock" >/dev/null 2>&1
fi
"$BT/apksigner" sign --ks "$KS" --ks-pass pass:android --key-pass pass:android \
    --out StatusClock.apk build/aligned.apk
echo "Built $(pwd)/StatusClock.apk"
