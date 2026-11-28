# Changelog

All notable changes to this fork. Upstream base: Open Camera 1.56.2 (commit 0dd4cbe).
Dates are Asia/Taipei.

Note (2026-11-28): the GitHub release tags for preset4–preset9 were zero-padded (now v1.56.2-preset04-release … v1.56.2-preset09-release) because GitHub sorts the releases list lexicographically and ranked "preset9" above "preset10". The app versionNames recorded in the headings below are unchanged.

## 1.56.2-preset10-release — 2026-11-27 (versionCode 107)

- Filename tag now chosen by value match across ALL presets, not only the "active" one: if the currently set ISO + exposure time exactly match a preset's stored values, that preset's tag is used (the active preset wins when several match). Previously, dialing a preset's exact values in manually — without tapping that preset's button, so another preset stayed "active" — produced no tag even though the photo was taken with that preset's values (on-device report: values matching Box 4 got no `_Box_4` tag). Values matching no preset still produce no tag.
- The collapsed pill now shows the currently selected values (with the active preset's name when one is active), e.g. "Box 4 ISO 100 ·1/50 ▶", instead of only the static "Presets ▶" label — after choosing a preset the bar auto-collapses, so the pill is what is actually seen.
- Live refresh: manual ISO/exposure changes made with the app's own popup/sliders write preference_iso / preference_exposure_time directly and bypass MainActivity.updateForSettings(), so the preset9 rebuild hook never fired for them. A SharedPreferences change listener now rebuilds the bar/pill on any relevant key change.
- Security pass: permission set identical to preset9-release, debuggable off, same release key, secret and de-identification scans clean.

## 1.56.2-preset9-release — 2026-11-27 (versionCode 106)

- The active preset's button now shows the currently selected camera values (live ISO + exposure time) instead of always showing the preset's stored values: after applying a preset and then adjusting ISO/exposure manually, the button reflects what is actually in effect. Inactive buttons keep showing their stored values, so presets can still be compared before choosing. The bar rebuilds on every settings change (hooked into MainActivity.updateForSettings), so the display stays current.
- Security pass: permission set and exported components identical to preset8-release, debuggable off, same release key, no sensitive APIs in the changed code, secret and de-identification scans clean, APK URL set unchanged.

## 1.56.2-preset8-release — 2026-11-23 (versionCode 105)

- Filename tags are now English-only, regardless of the UI language: the default tag is the canonical `_Box_<index+1>` (so a zh-rTW phone now produces `IMG_20261123_120000_Box_2.jpg` instead of `_控箱2`). A custom preset name is reduced to ASCII letters/digits/"_"/"-", and a custom name containing no ASCII letter falls back to `_Box_<index+1>`. The preset-match rule from preset7 is unchanged.
- Security pass: permission set and exported components identical to preset7-release, debuggable off, same release key, no sensitive APIs in the changed code, secret and de-identification scans clean, APK URL set unchanged.

## 1.56.2-preset7-release — 2026-11-23 (versionCode 104)

- New: photo filenames are tagged with the active preset, e.g. `IMG_20261123_120000_控箱2.jpg` (English locale: `_Box_2`). The tag is added only when a preset is active AND the currently set ISO + exposure time still exactly match that preset's stored values, so the filename never claims a preset the photo was not taken with; manually changing ISO/exposure after applying a preset drops the tag. The preset name is sanitized for filenames (invalid characters and spaces become "_"). Videos and burst sequences are not tagged.
- Security pass: permission set and exported components identical to preset6-release, debuggable off, same release key, no sensitive APIs in the changed code, secret and de-identification scans clean, APK URL set unchanged.

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
