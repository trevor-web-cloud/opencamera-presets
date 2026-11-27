# Open Camera Presets (fork)

A fork of **Open Camera 1.56.2** that adds an on-screen **preset bar** for quickly applying ISO + exposure-time combinations while shooting.

Built for a repetitive inspection workflow: several pieces of equipment each need a known manual ISO / shutter-speed pair, and switching through the exposure popup menu for every shot is too slow. With this fork, each combination is one tap away.

## The preset bar

- A horizontally scrollable bar of preset buttons sits above the shutter button: **Box 1, Box 2, Box 3, Box 4, ...** (named per equipment cabinet in the original use case; the label follows the system language, e.g. Traditional Chinese shows 控箱1, 控箱2, ...).
- **Tap** a preset: applies its stored ISO and exposure time through the same code path as the built-in exposure menu, and highlights the active preset. The active preset's button shows the **currently selected** camera values (since preset9), so it stays truthful after manual adjustments; inactive buttons show their stored values.
- **Long-press** a preset: stores the currently set manual ISO and exposure time into that button, so presets are calibrated on site. Saving is refused while ISO is set to Auto.
- **+ Add** appends another preset (Box N+1). The count and values persist in the app's preferences.
- After a preset is applied, the bar **auto-collapses** to a small **"Presets ▶"** pill docked below the top toolbar, so it never blocks the preview or the focus point while framing. Tap the pill to expand the bar again; the **◀** button collapses it manually. The collapsed state persists across restarts.
- Scope: a preset stores **ISO + exposure time only** (not white balance, focus, or other settings).
- **Filename tag (preset7, English-only since preset8):** photos taken while a preset is applied are saved with an English preset tag appended, e.g. `IMG_20261123_120000_Box_2.jpg`, whatever the UI language. A custom preset name is reduced to ASCII, and a name with no ASCII letter falls back to `Box_<index+1>`. The tag appears only while the current ISO + exposure time still exactly match the preset's stored values; changing them manually after applying drops the tag, so a tagged filename is proof of the settings used. Videos and burst sequences are not tagged.

## Preference key convention (read this before parsing a settings backup)

The fork's preset values live in the standard Open Camera preferences, so
they appear in Settings-backup XML exports (`BACKUP_OC_*.xml`). The key
suffix is the **zero-based index**, while the on-screen name is index + 1:

| Key in backup file | Meaning |
|---|---|
| `preference_preset_iso_0` / `preference_preset_exposure_time_0` | Box 1 |
| `preference_preset_iso_1` / `preference_preset_exposure_time_1` | Box 2 |
| `preference_preset_iso_N` / `preference_preset_exposure_time_N` | Box N+1 |
| `preference_preset_active` (int) | Active preset index (0-based; `1` = Box 2) |
| `preference_preset_count` (int) | Preset count; only written after "+ Add" is used, otherwise the default of 4 applies and the key is absent from backups |
| `preference_preset_name_N` | Optional custom name for Box N+1; absent means the localized default name |

Exposure times are stored as nanoseconds (e.g. `100000000` = 1/10 s).
Do not read the suffix as the box number: `_1` is Box 2, not Box 1.

## Install

Download the APK from the [Releases](../../releases) page and sideload it.

- Package name is `net.sourceforge.opencamera.presets`, so this fork **installs side by side** with the official Open Camera and never overwrites it.
- Release builds are signed with the fork's own key (certificate SHA-256 `8db3498b4f38e40655c0135f91d43dd00fe7599e50bc77e99435219dccc4d55b`) and have `debuggable` off. Verify downloads against the SHA-256 published in each release's notes.
- Note: the early preset1–preset3 builds were debug-signed. Switching from a debug build to a release build requires uninstalling first (stored presets are cleared); upgrades between release builds keep them.

## Build

Standard Android Gradle build of the upstream tree:

```
./gradlew assembleRelease
```

Release APKs are then zipaligned and signed with `apksigner` (signing key not included in this repository).

## Changes relative to upstream

Kept deliberately small — 7 files touched, 1 file added:

| File | Change |
| --- | --- |
| `app/src/main/java/net/sourceforge/opencamera/ui/PresetBar.java` | **New.** Builds the preset bar/pill, apply/save logic |
| `app/src/main/java/net/sourceforge/opencamera/ui/MainUI.java` | Hooks the bar and the top pill into `layoutUI()` |
| `app/src/main/java/net/sourceforge/opencamera/PreferenceKeys.java` | Preference keys for preset count, values, active preset, collapsed state |
| `app/src/main/res/layout/activity_main.xml` | `preset_bar` scroll view and `preset_pill` button |
| `app/src/main/res/values/strings.xml`, `app/src/main/res/values-zh-rTW/strings.xml` | Preset bar strings (English default, Traditional Chinese) |
| `app/build.gradle`, `app/src/main/AndroidManifest.xml` | Package name `net.sourceforge.opencamera.presets`, fork version names |

## Attribution / upstream

This project is a derivative work of **Open Camera**, developed by **Mark Harman**.

- Upstream website: https://opencamera.org.uk/
- Upstream source: https://sourceforge.net/projects/opencamera/ (git: `https://git.code.sf.net/p/opencamera/code`)
- Base version: **1.56.2**, upstream commit `0dd4cbe78872df2c6e4eb6cee3fb0d5637b0f52e`
- Open Camera is © 2013–2026 Mark Harman and is released under the **GNU General Public License v3 or later** (see `gpl-3.0.txt`). This fork is distributed under the same license. All upstream copyright and license notices are preserved unchanged.
- Icons from Google's Material Design icons (Apache License 2.0) and the AndroidX/Jetpack libraries (Apache License 2.0), as in upstream.

## Upstream sync policy

This fork tracks upstream Open Camera releases: when a new upstream version is published, the preset-bar changes are rebased onto it and a new fork release is issued, so the fork does not drift behind upstream fixes.

## Version history

See [CHANGELOG.md](CHANGELOG.md).
