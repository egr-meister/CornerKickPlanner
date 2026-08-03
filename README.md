# CornerKick Planner

CornerKick Planner is a native Android app for planning football **corner-kick
routines**. Create set-piece schemes, choose a corner type, place player markers
on a top-down pitch board, draw simple movement arrows, add tactical notes, and
save, browse, edit, duplicate, and delete your schemes. A secondary **Match
Schedule** screen loads upcoming fixtures from football-data.org as an optional
reference.

The app is built with Kotlin and Jetpack Compose, uses Material 3, and stores
all user data locally on the device.

---

## Table of contents

1. [Project description](#project-description)
2. [Features](#features)
3. [Tactics disclaimer](#tactics-disclaimer)
4. [Match Schedule API disclaimer](#match-schedule-api-disclaimer)
5. [football-data.org API v4](#football-dataorg-api-v4)
6. [Default 10-day API window](#default-10-day-api-window)
7. [`local.properties` setup](#localproperties-setup)
8. [What NOT to include](#what-not-to-include)
9. [Local storage & DataStore](#local-storage--datastore)
10. [Corner schemes, markers, arrows, notes, history](#core-concepts)
11. [Match Schedule (secondary feature)](#match-schedule-secondary-feature)
12. [App icon, splash & visual style](#app-icon-splash--visual-style)
13. [Open in Android Studio](#open-in-android-studio)
14. [Build the app](#build-the-app)
15. [Generate a PKCS12 keystore](#generate-a-pkcs12-keystore)
16. [GitHub Secrets & GitHub Actions](#github-secrets--github-actions)
17. [Google Play & Android 15/16 KB notes](#google-play--android-1516-kb-notes)
18. [Release optimization](#release-optimization)
19. [Local launch verification checklist](#local-launch-verification-checklist)
20. [Privacy note](#privacy-note)

---

## Project description

The main purpose of CornerKick Planner is **corner-kick tactical planning**. The
home screen answers a single question: *"What corner-kick routine do I want to
build or review?"* The app is a manual planner — every scheme, marker, arrow and
note is created by the user. It is **not** a live-score app, a betting app, a
streaming app, or an official football product.

## Features

- Create a corner-kick scheme (title, date, corner type, attack side, notes).
- Choose a corner type: **Near Post**, **Far Post**, **Short Corner**, or **Custom**.
- Choose an attack side: **Left Corner** or **Right Corner**.
- Place player markers on a top-down corner board: **Attacker, Defender,
  Goalkeeper, Ball, Target** — with a team-color role (Attack / Defense / Neutral)
  and a short label.
- Draw simple movement arrows: **Run, Pass, Cross, Dummy Move, Pressing Arrow**,
  each defined by a start and end point (tap-to-place, no fragile drag-and-drop).
- Add tactical notes to any scheme.
- Save multiple schemes and browse them in reverse-chronological history.
- Filter history by corner type and attack side.
- Open, edit, duplicate, and delete saved schemes.
- Secondary **Match Schedule** screen powered by football-data.org API v4.
- Works fully offline for planning; internet is only used by Match Schedule.

## Tactics disclaimer

> CornerKick Planner is a manual football tactics planner. Corner schemes,
> player markers, arrows, and notes are created by the user. The app is not an
> official football tool and does not provide professional coaching advice.

This note appears in onboarding, in Settings, and here in the README.

## Match Schedule API disclaimer

> Match data is provided by football-data.org. Availability, accuracy,
> competitions, and update frequency depend on the API provider and the current
> API plan.

This note appears on the Match Schedule screen, in Settings, and here.

## football-data.org API v4

The optional Match Schedule feature reads fixtures from the football-data.org
API v4.

- Base URL: `https://api.football-data.org/v4`
- Endpoint used: `GET /matches`
- Full example: `https://api.football-data.org/v4/matches?dateFrom=<today>&dateTo=<todayPlus9Days>`
- Auth header: `X-Auth-Token: <your token>`

Only the read-only `/matches` endpoint is used. The app never calls odds,
predictions, bookmakers, betting, or live-betting endpoints. All API access is
isolated in `data/remote/FootballDataRepository.kt`.

Get a free token at <https://www.football-data.org/>. The free plan is rate
limited, so the app **does not poll**: it refreshes only on manual action or
once per screen open when the cached window is out of date, and caches the last
successful response locally.

## Default 10-day API window

By default the Match Schedule feature requests a **10-day** window:

- `dateFrom` = today (device date), formatted `YYYY-MM-DD`
- `dateTo` = today + 9 days, formatted `YYYY-MM-DD`

Dates are computed locally from the device date; nothing is hard-coded. You can
set a custom window in Match Schedule settings (kept small and validated), or
leave both dates empty to use the default window.

## `local.properties` setup

The API token is provided through `local.properties`, which is **git-ignored**
and must **never** be committed.

1. Copy the example file:
   ```bash
   cp local.properties.example local.properties
   ```
2. Edit `local.properties`:
   ```properties
   FOOTBALL_DATA_API_TOKEN=your_real_token_here
   FOOTBALL_API_BASE_URL=https://api.football-data.org/v4
   ```
3. These values are exposed to the app as `BuildConfig.FOOTBALL_DATA_API_TOKEN`
   and `BuildConfig.FOOTBALL_API_BASE_URL` (see `app/build.gradle.kts`). The
   token is **never hard-coded** in source.

If the token is **missing, empty, or still `your_api_token_here`**, the app
still runs: the Match Schedule screen shows built-in **demo match data** with a
friendly message, and the rest of the app is unaffected.

> **Never commit your API token.** It must not appear in source, README,
> screenshots, tests, or GitHub Actions logs.

## What NOT to include

This app intentionally contains **none** of the following: ads, analytics,
payments, account registration, cloud sync, Firebase, betting odds, bookmaker
data, predictions, gambling / casino / prize / jackpot / deposit / balance /
freebet language, official club or league logos, FIFA/UEFA/Premier
League/LaLiga/Champions League branding, real player photos, video streaming or
highlights, or any "Te Apuesto" name, logo, or branding. There is **no**
automatic tracking of any kind. Competition and team names shown on the Match
Schedule screen are plain text returned by the API.

## Local storage & DataStore

All user data is stored **locally** using **DataStore Preferences**, serialized
as a single JSON string with **Kotlinx Serialization**. No database (Room) is
used — simple JSON in DataStore is sufficient.

Stored data: corner schemes (with pitch schemes, markers and arrows), user
settings, onboarding flag, match schedule settings, the cached match schedule,
last API update time, last API error message, and the last requested match
`dateFrom` / `dateTo`.

Safety: loaded data is merged with defaults; empty storage, missing fields,
unknown fields, and corrupted JSON all fall back to safe defaults so the app
never crashes. Empty lists (schemes / markers / arrows) and missing selections
are handled everywhere.

## Core concepts

**Corner schemes** hold the metadata (title, date, corner type, attack side,
notes) plus a pitch scheme.

**Markers** represent players and objects on the pitch. Each has a type
(Attacker, Defender, Goalkeeper, Ball, Target), a team-color role, a short
label, and relative `x`/`y` coordinates in the range `0.0–1.0`.

**Arrows** represent movement. Each has a type (Run, Pass, Cross, Dummy Move,
Pressing Arrow), relative start/end coordinates, and an optional label. Arrows
are created by tapping a start point then an end point — no freehand drawing.

**Notes** are plain local text (e.g. *"Near post run by striker."*).

**Scheme history** lists saved schemes newest-first, with type/side filters and
open/edit/duplicate/delete actions.

## Match Schedule (secondary feature)

The Match Schedule screen is an **extra reference** and never the identity of
the app. On open it shows cached matches immediately, refreshes once if the
cached window is stale, and offers a manual **Refresh** button. It shows the
active date window (labeled "Today + 9 days" when default), the last-updated
time, a source indicator (Live / Cached / Demo), and friendly messages for no
token, no internet, API limit reached, or an invalid response. Match cards show
home/away team names, date, time, competition name/code, status, and a score
only when the API provides one.

## App icon, splash & visual style

- **App icon:** a custom adaptive icon — rounded strong-orange background with a
  black corner-flag and a white ball. Readable at small sizes. No official
  logos, club branding, player photos, or betting symbols. PNG fallbacks are
  provided for API 24–25 and an adaptive icon for API 26+.
- **Splash screen:** a custom splash (via `androidx.core:core-splashscreen`) — a
  strong-orange background with the corner-board icon.
- **Visual style — "Orange Set-Piece Board":** bold strong orange, black/dark
  graphite contrast, and white content cards. Green is used only for pitch
  elements. The layout is a corner-kick tool with a strong orange header, dark
  tactical panels, marker chips, arrow controls, and a small secondary Match
  Schedule card — deliberately not a betting/live-score/dashboard template.

## Open in Android Studio

1. Install a recent Android Studio (Koala or newer recommended).
2. **File → Open** and select the `CornerKickPlanner` folder.
3. Let Gradle sync. Android Studio will generate the Gradle wrapper
   (`gradle/wrapper/gradle-wrapper.jar`) automatically on first sync.
4. Create `local.properties` (see above) and add your API token if you have one.
5. Run the `app` configuration on a device or emulator (Android 7.0 / API 24+).

> **Note on the Gradle wrapper jar:** the binary `gradle-wrapper.jar` is not
> committed. Android Studio generates it on first sync. From the command line
> you can generate it once with a local Gradle install:
> `gradle wrapper --gradle-version 8.9`. After that, `./gradlew` works.

## Build the app

With the wrapper present (after Android Studio sync or `gradle wrapper`):

```bash
# Debug build
./gradlew :app:assembleDebug

# Release APK (needs signing env vars — see below)
./gradlew :app:assembleRelease

# Release AAB (Google Play upload target)
./gradlew :app:bundleRelease
```

Release signing reads these environment variables (never committed):
`ANDROID_KEYSTORE_FILE`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`,
`ANDROID_KEY_PASSWORD`. If they are not set, the release build is produced
unsigned locally so the project always builds; CI always signs with the real
keystore.

## Generate a PKCS12 keystore

Release builds (APK and AAB) must be signed with a **real PKCS12 keystore**, not
a debug key. Generate one with:

```bash
keytool -genkeypair -v -storetype PKCS12 \
  -keystore cornerkick-planner-release-key.p12 \
  -alias cornerkick_planner_key \
  -keyalg RSA -keysize 2048 -validity 10000
```

Use the **same password** for the keystore and the key. Keep the `.p12` file and
its passwords private — they are git-ignored and must never be committed.

## GitHub Secrets & GitHub Actions

The workflow at `.github/workflows/android-build.yml` runs on push to `main`. It:

1. Sets up JDK 17 and the Android SDK.
2. Installs `platforms;android-35` and `build-tools;35.0.0`.
3. Writes `local.properties` from the optional `FOOTBALL_DATA_API_TOKEN` secret
   (placeholder if unset — the build still succeeds and the app uses demo data).
4. Decodes the release keystore from a secret.
5. Builds the signed release **APK** and **AAB**.
6. Runs `apksigner verify --print-certs` on the release APK, prints the signing
   certificate, and **fails the build if the certificate contains
   `CN=Android Debug`** (prevents a Play rejection for a debug-signed artifact).
7. Uploads the APK and AAB as build artifacts.

Add these repository **Secrets** (Settings → Secrets and variables → Actions):

| Secret | Description |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64 of your `.p12` keystore: `base64 -i cornerkick-planner-release-key.p12` (macOS) or `base64 -w0 ...` (Linux) |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Key alias (e.g. `cornerkick_planner_key`) |
| `ANDROID_KEY_PASSWORD` | Key password (same as keystore password) |
| `FOOTBALL_DATA_API_TOKEN` | *(optional)* football-data.org token; if omitted the app uses demo data |

The real token and keystore passwords are only ever read from Secrets and are
never printed to logs.

## Google Play & Android 15/16 KB notes

- `compileSdk` and `targetSdk` are **35** (Android 15); `minSdk` is **24**.
- The **AAB** (`app-release.aab`) is the Google Play upload target — not the APK.
- The app ships **no native libraries**, so it is compatible with the Android 15+
  **16 KB memory page size** requirement out of the box.
- The manifest declares only the **INTERNET** permission.

## Release optimization

R8 / shrinking is enabled for release in `app/build.gradle.kts`:

```kotlin
isMinifyEnabled = true
isShrinkResources = true
```

If you want to validate a non-minified release first (recommended before your
very first store upload), temporarily set both to `false`, build and launch-test,
then re-enable them and re-test. Keep rules in `app/proguard-rules.pro` (they
preserve Kotlinx Serialization models and Retrofit interfaces).

## Local launch verification checklist

A green CI build is not proof the app launches. Before releasing:

```bash
adb install app/build/outputs/apk/release/app-release.apk
adb logcat
```

Then verify there are **no** crashes (ClassNotFound, NoSuchMethod,
serialization, DataStore JSON parse, missing navigation argument, missing
scheme/marker/arrow, invalid corner type/side/date, invalid API response,
missing API token, signature misconfiguration) while you:

- launch first-run with empty storage and complete onboarding;
- create Near Post, Far Post, and Short Corner schemes;
- add / edit / delete markers; add / delete arrows; edit notes; save;
- duplicate and delete schemes; filter history by type and by side;
- open Match Schedule with **no** token (demo data) and with a token;
- confirm the default request uses **today + 9 days**;
- refresh matches manually; simulate API failure; check cached matches;
- clear match cache; reset all local data; relaunch; launch in airplane mode;
- verify the release APK signature; confirm **only INTERNET** permission is used
  (no location, camera, microphone, contacts, storage, notifications, sensors,
  Google Fit, Health Connect, or wearable permissions).

## Privacy note

> CornerKick Planner stores tactical schemes, markers, arrows, notes, settings,
> and cached match data on this device. The app uses internet only to load
> football match data from football-data.org. No account, no ads, no analytics,
> no payments, no Firebase, no location, no notifications, no sensors, no Google
> Fit, and no Health Connect.
