# Agent instructions for point-app

This file is for AI coding agents (Claude Code, Codex CLI, Cursor, etc.) that
clone this repo to make changes. It gives the setup and build steps needed to
get a working build with no back-and-forth with a human, plus the constraints
that matter for this codebase.

## What this project is

Point is being migrated from React Native/Expo to **Kotlin Multiplatform +
Compose Multiplatform**, targeting Android and iOS. The RN code (`App.tsx`,
`app/`, `package.json`, etc.) is reference-only — do not extend it, and do not
delete it until the KMP app reaches visual parity. All new work goes into the
KMP modules: `shared/` (commonMain + platform source sets) and `androidApp/` /
`iosApp/`.

Read `MIGRATION_PLAN.md`, `KMP_ARCHITECTURE.md`, and `UI_SPEC.md` at the repo
root before making non-trivial changes — they are the design/architecture
source of truth for this migration, not this file.

## One-time setup

1. **JDK 21** and an **Android SDK** (compileSdk 36, so a recent SDK Platform
   + Build-Tools install) must be available.
2. Copy `local.properties.example` to `local.properties` and fill in:
   - `sdk.dir` — path to your Android SDK.
   - `GEMINI_API_KEY` — optional, dev-only, see comments in the file. Leave
     blank unless you're specifically working on `DirectGeminiLiveSession`.
3. Get `androidApp/google-services.json` (Firebase Android config for
   `com.point.repair`, project `point-74ae6`). It's git-ignored and not in
   this repo. Ask the repo owner for it, or download it yourself from the
   Firebase console if you have access
   (Project Settings → General → Point Android app). Without this file the
   build fails at `:androidApp:processDebugGoogleServices`.
4. (iOS only) `firebase/GoogleService-Info.plist` is likewise git-ignored and
   required for `iosApp` builds — same source as above.

## Build

From the repo root:

```
./gradlew :androidApp:assembleDebug
```

Output APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

Tests:

```
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test   # macOS only
```

iOS app: open `iosApp/` in Xcode and run from there (macOS + Xcode required;
not buildable from Linux/Windows).

## Toolchain constraints — do not bump without checking

- **AGP is pinned to 8.13.2** (`gradle/libs.versions.toml`). Do not upgrade
  Compose Multiplatform past the 1.9.x line without checking AGP compatibility
  first — Compose Multiplatform 1.11 pulls an androidx-compose version that
  hard-requires AGP 9.1, which is a canary-IDE-only requirement the project
  environment does not support. If a task seems to need AGP 9, stop and flag
  it instead of upgrading.
- Kotlin is pinned to 2.2.x, Haze (glass/blur) to 1.6.x. Keep these versions
  unless a task explicitly asks you to upgrade them.

## Runtime gotchas

- **App Check**: debug builds use `DebugAppCheckProviderFactory`
  (`androidApp/src/debug/kotlin/point/app/AppCheck.kt`), which generates a
  random debug token on first launch and logs it to logcat
  (`FirebaseAppCheck`/`DebugAppCheckProviderFactory` tag). Firebase-backed
  calls (Firebase AI Logic / Gemini Live in production mode) will be rejected
  by App Check until that token is registered in the Firebase console under
  App Check → Manage debug tokens for the `point-74ae6` project. This is a
  one-time step per install/emulator, not a code change.
- The AI loop is Gemini Live **exclusively**, not any other model family. In
  production it goes through Firebase AI Logic (`AiLiveSession`, model
  `gemini-2.5-flash-native-audio-preview-12-2025` as of this writing). There is
  a second, dev-only direct transport (`DirectGeminiLiveSession`) gated behind
  the optional `GEMINI_API_KEY` in `local.properties` — never wire a feature
  to depend on that path being present in a normal build.
- UI must match the Compose screens' existing spacing/type/gradient/glass
  treatment exactly — this project treats visual fidelity as a first-class
  requirement, not polish to skip. See `UI_SPEC.md`.

## What not to do

- Don't commit `google-services.json`, `GoogleService-Info.plist`,
  `local.properties`, or any API key — all are git-ignored on purpose.
- Don't add fallback/mock implementations for missing Firebase config to make
  the build "succeed" without it — get the real config file instead (see
  Setup above).
- Don't touch the RN code (`App.tsx`, `app/`) as part of KMP feature work.
