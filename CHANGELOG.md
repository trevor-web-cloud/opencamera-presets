# Changelog

All notable changes to this fork. Upstream base: Open Camera 1.56.2 (commit 0dd4cbe).
Dates are Asia/Taipei.

## 1.56.2-preset6-release — 2026-11-22 (versionCode 103)

- Calibration wizard revised after on-device feedback: (1) no preset (box) name is designated any more — the wizard is a single run, not a walk through named presets, and the banner no longer shows a preset name; (2) the bracket base is adjustable — the wizard starts from the current manual ISO + exposure time (Auto ISO is rejected with a hint), and a new "Set as base" button below the banner re-bases the remaining steps on whatever values are currently dialed in with the normal exposure controls. Ending the wizard restores the settings as they were when it started.
- Security pass: permission set and exported components identical to preset5-release, debuggable off, same release key, no sensitive APIs in the changed code, secret and de-identification scans clean, APK URL set unchanged.

## 1.56.2-preset5-release — 2026-11-22 (versionCode 102)

- New: guided calibration re-shoot wizard for phone changes. A "Calibrate" button at the end of the preset bar walks through every preset that has saved values: ISO stays at the saved value while the exposure time steps through a fixed bracket (saved value, +0.5 EV, +1 EV, -0.5 EV, -1 EV; five shots per preset). A banner below the top pill shows the current preset and step; tap it to advance after each shot, long-press to end. Finishing or ending always restores the preset's exact saved values. Wizard state persists across rotation/restarts. Capture stays manual on purpose: the photographer keeps framing control for every shot, the app only changes the exposure step and tracks progress.
- All wizard strings are localized (English / zh-rTW) like the rest of the preset bar.
- Security pass before release (standing rule: every revision is scanned before the GitHub release): permission set and exported components identical to preset4-release, debuggable off, same release key, no sensitive APIs in the new code, secret scan and de-identification scan clean, APK URL set unchanged.

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
