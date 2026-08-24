# Touch Grass

Offline single-screen Android installation. See [spec.md](spec.md).

Idle screen says **TOUCH GRASS** over a photograph of grass. Any touch replaces it
with **YOU TOUCHED GLASS. / THAT IS NOT GRASS.** and directions to the real patch.
It returns to idle 10 seconds later.

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

Use `600x1024` for the 7-inch tablet, `800x1280` for the 10-inch. Density varies
by device and is worth matching -- the K10C reports 213, not the 160 that
`avdmanager` defaults to. Confirm with `adb shell wm size` and `adb shell wm
density` after boot -- those report what the emulator actually applied, not what
the file asked for.

## Install

    adb install -r app/build/outputs/apk/debug/app-debug.apk

Launch it once from the launcher, or:

    adb shell am start -n com.debedb.touchgrass/.MainActivity

## Tablet setup

Airplane mode on. Automatic updates and notifications off. The app declares no
permissions and requests no network access.

The app is a normal launcher entry. If someone exits it, relaunch it from the
home screen; that is the accepted fallback rather than locking the tablet down.

## Customizing the installation

Everything installation-specific is a resource. Edit, rebuild, reinstall.

| What | Where |
|---|---|
| All on-screen text | `app/src/main/res/values/strings.xml` |
| Reset delay | `app/src/main/res/values/integers.xml` (`reset_delay_ms`) |
| Colors | `app/src/main/res/values/colors.xml` |
| Grass photograph | `app/src/main/res/drawable/grass.jpg` |
| Route map | `app/src/main/res/drawable-nodpi/route_map.png` |
| QR code | `app/src/main/res/drawable-nodpi/qr_directions.png` |

`destination_text` ships as `Replace with final directions`. Set it before the
installation goes up.

### Regenerating the QR code

The QR is a bundled PNG, not generated at runtime -- the app has no network
permission and no QR library. The URL it encodes is recorded in
`strings.xml` as `directions_url` purely so the code is reproducible:

    brew install qrencode
    qrencode -o app/src/main/res/drawable-nodpi/qr_directions.png \
        -s 16 -m 3 -l M "$URL"

Keep the URL short. Module count drives scannability: an 80-character URL is 37
modules across, while a full Google Maps share URL with its `data=` blob and
`g_ep` session token runs 357 characters and 73 modules -- roughly half the
pixels per module on the same screen. Verify after any change by decoding a real
screenshot, not just the source PNG:

    adb exec-out screencap -p > /tmp/shot.png
    zbarimg --raw /tmp/shot.png

`drawable-nodpi` matters for both images: in plain `drawable/` Android treats
them as mdpi and upscales on the 213 dpi tablet, which blurs QR modules.

`grass.jpg` is a photograph of the actual patch the directions point to -- Kings
Creek Meadow, Lassen Volcanic National Park. It is CC BY-SA 4.0 and the credit
in [CREDITS.md](CREDITS.md) must travel with it, including onto the physical
label. Replacing it with a different photo means updating that file too.
Shoot or crop it portrait, roughly 3:4; it is drawn `centerCrop` so the edges are
trimmed to fill the width.

Text auto-sizes to fit its box, so longer directions shrink rather than clip.

## Not built

- **Launch on boot.** The spec lists it as optional with manual launch as an
  acceptable fallback, and manual relaunch is what this installation uses. Making
  the app the Home app (`category.HOME`) would cover boot launch with no extra
  permission, but it also swallows the Home button, which makes the tablet
  awkward to administer on site. Rejected for that reason.
- **Sensing a touch on the astroturf.** Considered a camera (rejected: dust on
  the lens, darkness, power draw, consent, and spec.md forbids camera use) and a
  USB pressure mat or foot pedal behind a HID keystroke. The mat needs USB host
  mode, and this tablet does not declare `android.hardware.usb.host`; its
  `musb-hdrc` controller is registered as a UDC, i.e. peripheral role. Bluetooth
  HID was rejected separately for battery drain and reliability. The astroturf
  instead carries a printed laminated card, and the idle screen points at it.
  See issue #1.
- **Kiosk / lock task mode.** Immersive mode hides the system bars, but a
  deliberate swipe still reveals them transiently and Home still works. Real
  lockdown needs device-owner provisioning via `adb shell dpm set-device-owner`.
  Visitors exiting the app is considered acceptable.
