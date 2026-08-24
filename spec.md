# Touch Grass — App Specification

**Version:** 0.1  
**Target device:** COLORROOM K10C 10-inch Android tablet, 800×1280 at 213 dpi, Android 13 (SDK 33), armeabi-v7a  
**Mode:** Offline, portrait, full-screen installation

## Purpose

Present a deliberately simple interactive trap:

1. The screen instructs the visitor to **TOUCH GRASS** and shows a photograph of real grass.
2. When the visitor touches the screen, the app points out that they touched glass, not grass.
3. It gives directions to a specific patch of real grass.
4. After a few seconds, it resets for the next visitor.

## Screen States

### 1. Idle

- Top area: large text

  **TOUCH GRASS**

- Remaining area: full-width photograph of the actual grass referenced by the directions.
- Below the photograph, a line reading **THAT IS NOT GRASS.** with an arrow
  pointing off the bottom edge at the physical astroturf sample on the table.
  This is a warning, not a punchline: at idle the visitor has touched nothing
  yet.
- The entire screen is tappable.
- No buttons, instructions, menus, animations, or OS chrome.

### 2. Response

Immediately after any tap, replace the idle screen with:

> **YOU TOUCHED GLASS.**  
> **THAT IS NOT GRASS.**
>
> **REAL GRASS:**  
> `[configurable directions, distance, or coordinates]`
>
> `[route map image]`
>
> **SCAN FOR DRIVING DIRECTIONS**  
> `[QR code encoding a driving-directions URL]`

- Use very large, high-contrast text. The headline pair is set smaller than the
  idle title so the map and QR code fit without crowding.
- The map and the QR code are pre-rendered images bundled in the APK. Neither is
  generated or fetched at runtime; the app stays offline and permission-free.
- Size the QR so it stays scannable on the target display. At 800px wide the
  bundled code is 37 modules across, roughly 7px per module.
- Ignore additional taps while this state is visible.
- Return automatically to the idle state after **10 seconds** -- long enough to
  read the directions and scan the QR code.

## Runtime Behavior

- Launch directly into the idle state.
- Lock orientation to portrait.
- Run in immersive full-screen mode.
- Hide the status bar, navigation bar, notifications, and application controls.
- Keep the screen awake while the app is running.
- Produce no sound and no vibration.
- Require no network connection.
- Request no network permission.
- Bundle the grass image and all text inside the APK.
- On app restart, process restart, wake, or recovery from an error, return to the idle state.
- Touch response should appear effectively immediately.

## Configuration

For version 0.1, configuration may be compile-time constants or a bundled local JSON file:

```json
{
  "idle_title": "TOUCH GRASS",
  "response_title": "YOU TOUCHED GLASS.",
  "response_subtitle": "THAT IS NOT GRASS.",
  "destination_label": "REAL GRASS:",
  "destination_text": "Replace with final directions",
  "qr_caption": "SCAN FOR DRIVING DIRECTIONS",
  "directions_url": "https://www.google.com/maps/dir/...",
  "reset_delay_ms": 10000,
  "grass_image": "grass.jpg",
  "route_map_image": "route_map.png",
  "qr_image": "qr_directions.png"
}
```

No settings screen or remote content system is required.

## Installation Behavior

Preferred:

- APK installable by ADB or direct sideloading.
- Optional automatic launch after device boot.

Acceptable fallback:

- Operator launches the app manually after powering on the tablet.

The tablet itself should be placed in airplane mode, with automatic updates and notifications disabled.

## Non-Goals

Do **not** add:

- accounts or sign-in;
- analytics or telemetry;
- backend services;
- internet content;
- camera or microphone use;
- touch counters;
- sound effects;
- transitions beyond an immediate state change;
- explanatory text about the concept;
- an administrative interface.

The physical astroturf sample and museum-style label are separate parts of the installation.

## Acceptance Criteria

1. A fresh launch shows the idle screen without setup or prompts.
2. A tap anywhere on the screen immediately shows the response screen.
3. The response screen resets to idle after approximately 10 seconds.
4. Additional taps do not extend or restart the timer.
5. The app remains usable through at least 100 consecutive interaction cycles.
6. The screen stays awake during operation.
7. No status bar, navigation bar, notifications, or other Android UI is visible during normal use.
8. The app works fully offline.
9. After a forced restart or process kill, reopening the app returns to the idle state.
10. All installation-specific text, the grass photograph, the route map, and the
    QR code can be replaced without redesigning the application.
11. The QR code decodes to the intended directions URL when scanned from the
    device screen.
