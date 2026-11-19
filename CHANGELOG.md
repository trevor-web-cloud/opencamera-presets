# Changelog

All notable changes to this fork. Upstream base: Open Camera 1.56.2 (commit 0dd4cbe).
Dates are Asia/Taipei.

## 1.56.2-preset4-release — 2026-11-19 (versionCode 101)

- Localization: all preset-bar strings moved to string resources and now follow the system language. English (default): "Box N", "Presets ▶", "+ Add", English toasts. Traditional Chinese (zh-rTW): unchanged wording. Other locales fall back to English.
- Signed with the fork release key; direct upgrade from preset3-release, presets preserved.

## 1.56.2-preset3-release — 2026-11-19 (versionCode 100)

- First release build: `debuggable` off, signed with a dedicated self-generated RSA-4096 key (certificate SHA-256 8db3498b4f38e40655c0135f91d43dd00fe7599e50bc77e99435219dccc4d55b, valid to 2054) instead of the Android debug key.
- Static security pass before this build: no INTERNET permission (the app has no network capability), permission set / exported components / file inventory / URL strings identical to a same-source vanilla build, no native libraries, no embedded secrets.
- Switching from the debug-signed builds requires one uninstall (stored presets are cleared).

## 1.56.2-preset3 — 2026-11-19 (versionCode 99, debug build)

- The collapsed "Presets ▶" pill moved from the bottom-left of the screen to below the top toolbar, in the black band above the preview, so nothing overlaps the photo area while framing. Expanded bar remains at the bottom; apply still auto-collapses.

## 1.56.2-preset2 — 2026-11-19 (versionCode 98, debug build)

- Fix for the preset bar covering the lower preview area (focus/positioning point not visible): applying a preset now auto-collapses the bar to the small pill; a ◀ button collapses it manually; collapsed state persists; slimmer buttons.

## 1.56.2-preset1 — 2026-11-19 (versionCode 97, debug build)

- Initial fork release. On-screen preset bar (Box 1...Box 4 by default, + Add to append): tap applies ISO + exposure time, long-press saves the current manual ISO + exposure time. Package renamed to net.sourceforge.opencamera.presets so it coexists with the official app. Permissions identical to upstream.
