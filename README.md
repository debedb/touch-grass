# Touch Grass

Offline single-screen Android installation. See [spec.md](spec.md).

Idle screen says **TOUCH GRASS** over a photograph of grass. Any touch replaces it
with **YOU TOUCHED GLASS. / THAT IS NOT GRASS.** and directions to the real patch.
It returns to idle 6 seconds later.

## Build

Requires a JDK (17+) and the Android SDK.

On a machine that has neither SDK nor `adb` yet (macOS):

    brew install --cask android-commandlinetools
    export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
    yes | sdkmanager --sdk_root="$ANDROID_HOME" --licenses
    sdkmanager --sdk_root="$ANDROID_HOME" \
        "platform-tools" "platforms;android-34" "build-tools;34.0.0"

`local.properties` is machine-specific and deliberately untracked. Create one
here pointing at whatever SDK that machine has:

    sdk.dir=/opt/homebrew/share/android-commandlinetools

Then:

    ./gradlew assembleDebug

APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

### Testing without hardware

An emulator sized to the tablet works for everything except real touch. Note
that `avdmanager` already writes `hw.lcd.*` defaults into `config.ini`, so
appending your own values leaves duplicate keys -- strip the originals first.

    sdkmanager --sdk_root="$ANDROID_HOME" \
        "emulator" "system-images;android-31;google_apis;arm64-v8a"
    echo no | avdmanager create avd -n touchgrass \
        -k "system-images;android-31;google_apis;arm64-v8a"

    CFG="$HOME/.android/avd/touchgrass.avd/config.ini"
    grep -vE "^(hw\.lcd\.width|hw\.lcd\.height|hw\.lcd\.density|skin\.name|skin\.path)=" \
        "$CFG" > "$CFG.tmp"
    cat >> "$CFG.tmp" <<'EOF'
    hw.lcd.width=800
    hw.lcd.height=1280
    hw.lcd.density=160
    EOF
    mv "$CFG.tmp" "$CFG"

    "$ANDROID_HOME/emulator/emulator" -avd touchgrass -no-snapshot -no-audio

Use `600x1024` for the 7-inch tablet, `800x1280` for the 10-inch; both report
160 dpi. Confirm with `adb shell wm size` after boot -- that reports what the
emulator actually applied, not what the file asked for.

## Install

    adb install -r app/build/outputs/apk/debug/app-debug.apk

Launch it once from the launcher, or:

    adb shell am start -n com.debedb.touchgrass/.MainActivity

## Tablet setup

Airplane mode on. Automatic updates and notifications off. The app declares no
permissions and requests no network access.

## Customizing the installation

Everything installation-specific is a resource. Edit, rebuild, reinstall.

| What | Where |
|---|---|
| All on-screen text | `app/src/main/res/values/strings.xml` |
| Reset delay | `app/src/main/res/values/integers.xml` (`reset_delay_ms`) |
| Colors | `app/src/main/res/values/colors.xml` |
| Grass photograph | `app/src/main/res/drawable/grass.jpg` |

`destination_text` ships as `Replace with final directions`. Set it before the
installation goes up.

The bundled `grass.jpg` is a generated placeholder texture, not a photograph of
any real patch. Overwrite it with the real photo at the same path and filename.
Shoot or crop it portrait, roughly 3:4; it is drawn `centerCrop` so the edges are
trimmed to fill the width.

Text auto-sizes to fit its box, so longer directions shrink rather than clip.

## Not built

- **Launch on boot.** The spec lists it as optional with manual launch as an
  acceptable fallback. Adding it needs a `RECEIVE_BOOT_COMPLETED` permission and
  a receiver. Say the word if the tablet will be power-cycled unattended.
- **Kiosk / lock task mode.** Immersive mode hides the system bars, but a
  deliberate swipe still reveals them transiently and Home still works. Real
  lockdown needs device-owner provisioning via `adb shell dpm set-device-owner`.
