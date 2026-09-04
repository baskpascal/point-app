# Point — Migration Plan: React Native/Expo → Kotlin Multiplatform + Compose Multiplatform

Companion docs: `KMP_ARCHITECTURE.md`, `UI_SPEC.md`, `MIGRATION_CHECKLIST.md`.

## Objectives

1. Ship Point as a high-quality cross-platform app (Android + iOS) on KMP +
   Compose Multiplatform.
2. Reproduce the existing Point UI at pixel fidelity — no reinterpretation, no
   Material defaults.
3. Preserve the camera-first / voice-first product: identify objects, understand
   manuals, diagnose problems, guide repairs step-by-step with live vision +
   voice.
4. Qualify for the RevenueCat Shipaton **Ship Kotlin Everywhere / JetBrains**
   category.

## Ground rules

- The React Native app under `app/` is **reference material**. Do not evolve it.
- **Do not delete the RN implementation** until the Compose version reaches
  visual parity (end of Phase 4, re-confirmed after Phase 11).
- No dual long-term maintenance — once parity is reached and the KMP app is the
  shipping target, the RN code moves to `legacy-rn/` and is removed after one
  stable release.
- UI fidelity is a feature. "Elements exist" ≠ "screen done" — see the Quality
  Bar below.

## Environment reality

- This repo is edited from WSL, which has **no JDK / Android SDK / Xcode**.
- All Gradle builds, Android device runs, and Kotlin/Native iOS builds happen on
  the **user's Windows machine (Android Studio)** and a **Mac** (for iOS).
- After each phase, the user builds + runs and reports back; discrepancies feed
  the next correction pass.

## Missing inputs (please provide)

| Input | Needed for | Status |
|---|---|---|
| Original design screenshots (6 screens) | Phase 4 visual comparison | **not in repo** — code tokens used as SoT meanwhile; drop in `design/references/` |
| `POINT_BUILD_SPEC.md` | cross-checking intent (§5.x referenced by token files) | in the Claude Project, not on disk — paste if strict adherence matters |
| Firebase project (`google-services.json`, `GoogleService-Info.plist`) | Phase 8 | needed before Phase 8 |
| Gemini access via Firebase AI Logic (enabled in Firebase console) | Phase 8 | needed before Phase 8 |
| RevenueCat project + API keys + `point_pro` entitlement + offerings | Phase 10 | needed before Phase 10 |
| Apple Developer account + bundle id | Phase 9 | `com.point.repair` reserved in `app.json` |

## RN → Compose artifact map

| RN artifact | Compose target |
|---|---|
| `app/theme/colors.ts` | `design/PointColors.kt` |
| `app/theme/gradients.ts` | `design/PointGradients.kt` |
| `app/theme/radii.ts` | `design/PointRadii.kt` |
| `app/theme/spacing.ts` | `design/PointSpacing.kt` |
| `app/theme/shadows.ts` | `design/PointElevation.kt` (blurred-layer shadow modifiers) |
| `app/theme/typography.ts` | `design/PointTypography.kt` + Inter font resources |
| `app/components/foundation/*` | `ui/foundation/*` |
| `app/components/branding/*` | `ui/branding/*` |
| `app/components/camera/*` | `ui/camera/*` |
| `app/components/guidance/*` | `ui/guidance/*` |
| `app/components/diagnosis/*` | `ui/diagnosis/*` |
| `app/components/timeline/*` | `ui/timeline/*` |
| `app/components/paywall/*` | `ui/paywall/*` |
| `app/screens/*` | `screen/*` |
| `app/navigation/*` | `nav/*` |
| `app/store/repairStore.ts` | `state/RepairSessionStore.kt` |
| `app/mocks/repairSession.ts` | `mock/MockRepairSession.kt` |
| `expo-blur` `BlurView` | Haze (`hazeSource` / `hazeEffect`) |
| `expo-linear-gradient` | `Brush.linearGradient` / `radialGradient` |
| `react-native-reanimated` | Compose `animate*AsState`, `rememberInfiniteTransition`, `Animatable` |
| `react-native-svg` (ConfidenceRing) | Compose `Canvas` + `drawArc` |
| `expo-haptics` | `expect fun hapticMedium()` (Android `HapticFeedbackConstants` / `VibrationEffect`, iOS `UIImpactFeedbackGenerator`) |
| `expo-camera` | `CameraController` + `CameraX` / `AVFoundation` |
| React Navigation native-stack | `navigation-compose` (Compose MP) |
| RevenueCat (not yet integrated in RN) | RevenueCat KMP SDK |

---

## Phases

Each phase lists **scope**, **exit criteria**, and **who verifies**. Detailed
task boxes are in `MIGRATION_CHECKLIST.md`.

### Phase 1 — Create the KMP + Compose Multiplatform project
**Scope:** Scaffold `:composeApp` (commonMain/androidMain/iosMain) + `iosApp/`
Xcode project via the Compose Multiplatform wizard / `kmp.jetbrains.com`
template. `libs.versions.toml` with pinned versions. Koin skeleton. App id
`com.point.repair`. RN files untouched (KMP lives alongside at repo root; Gradle
files added).
**Exit:** `./gradlew :composeApp:assembleDebug` succeeds on Windows; blank
"Point" screen runs on an Android device and the iOS simulator.
**Verify:** user (Android Studio + Mac).

### Phase 2 — Theme & design tokens
**Scope:** Port every token from `UI_SPEC.md` §1 into `design/`. `PointTheme`
composable + CompositionLocals. Inter font resources. `softShadow`/`glowShadow`
modifiers. `PointGradients`. Haze `PointGlass` styles (initial values). A
`DesignPreviewScreen` swatch page (colors, type ramp, radii, shadows, one glass
card over a photo).
**Exit:** swatch page matches `UI_SPEC.md` §1 exactly; glass card shows real
blur over a background image on both platforms.
**Verify:** user, side-by-side with UI_SPEC values.

### Phase 3 — Rebuild all six screens with mocked state
**Scope:** `MockRepairSession.kt` (port of `repairSession.ts`). All foundation +
screen components (`ui/*`). All six `screen/*` composables wired to mock data,
statically (no nav yet — a debug switcher to jump between screens). Live camera
screens use a **static placeholder image** as the camera layer so glass/haze is
testable without CameraX.
**Exit:** every screen renders with mock content; Live Guidance has **no opaque
panel** and the placeholder image is visible behind floating elements.
**Verify:** user.

### Phase 4 — Visual comparison & correction  *(parity gate — RN may not be deleted before this passes)*
**Scope:** For each screen: capture Compose screenshot (Android + iOS), compare
against (a) the RN app running on the same device and (b) design screenshots
when provided. Walk the `UI_SPEC.md` §5 checklist. File a discrepancy list;
correct spacing, type metrics, glass opacity/blur, shadow softness, radii,
gradient stops/direction, icon + touch-target sizes, animation curves/durations.
Iterate until each screen passes.
**Exit:** all six screens pass the §5 checklist; discrepancy list empty or
explicitly deferred with sign-off.
**Verify:** user (this is the primary gate).

### Phase 5 — Navigation & repair state
**Scope:** `navigation-compose` host, routes = `RootStackParamList`, fade
transitions, `Premium` modal slide-up. `RepairSessionStore` as the single
`StateFlow`, seeded from mock, exposed via view model / Koin. Wire all screen
actions (`Start a repair`, mic press, `Continue step`, `Guide me`, plan select,
back/close) to real navigation + store intents. `advanceStep`, `setActiveStepIndex`,
`setListening`, `selectPlan` behave as in `repairStore.ts`.
**Exit:** full flow navigable Splash → Camera → Guidance → Timeline → Diagnosis →
Premium and back; state persists across rotation.
**Verify:** user.

### Phase 6 — Android live camera
**Scope:** `CameraXController` (preview + throttled ~1 fps JPEG frame stream +
permission flow). `CameraPreview.android.kt` via `AndroidView(PreviewView)`.
Replace the placeholder image on Live Camera / Live Guidance with the real
preview; Haze `hazeSource` now targets the live `PreviewView`. Permission-denied
fallback matches RN (`#12151A` + prompt text).
**Exit:** live camera renders full-bleed on Android with glass overlays blurring
real video at 60fps UI; frames Flow emits ~1/sec.
**Verify:** user (Android device).

### Phase 7 — PCM microphone / player (Android)
**Scope:** `AndroidLiveAudioEngine` — `AudioRecord` capture → `Flow<PcmChunk>`
(16 kHz mono 16-bit); `AudioTrack` streaming playback (24 kHz). Echo
cancellation where available. A local loopback/debug harness (record 3s → play
back) to validate formats before wiring the AI.
**Exit:** clean capture + playback on an Android device; measured sample
rate/format correct; no buffer underruns in a 60s test.
**Verify:** user (Android device, headphones).

### Phase 8 — Firebase AI Logic / Gemini Live integration
**Scope:** Firebase init + App Check (Android). `FirebaseAiLiveSession` (Android)
implementing `AiLiveSession`: connect to `gemini-3.1-flash-live-preview`, stream
audio + ~1 fps frames, receive audio + transcripts, `set_repair_state` function
tool. `DefaultRepairSessionRepository` orchestrates camera + audio + AI → store;
handles interruption (`clearPlayback`), context compression, `GoAway` +
session resumption. Typed-text fallback (`sendUserText`). System instruction
encodes the Point product role + when to call `set_repair_state`.
**Exit:** end-to-end on Android — point at an object, speak, hear a spoken
answer, see transcript capsules, and the Step pill / instruction / diagnosis
update from real model output. Survives a >2 min session.
**Verify:** user (Android device).

### Phase 9 — iOS platform adapters
**Scope:** `AVFoundationCameraController`, `CameraPreview.ios.kt` (`UIKitView` +
`AVCaptureVideoPreviewLayer`), `IosLiveAudioEngine` (`AVAudioEngine` capture +
`AVAudioConverter` to 16 kHz, `AVAudioPlayerNode` 24 kHz), `FirebaseAiLiveSession`
(FirebaseAI Apple SDK), App Check (DeviceCheck/AppAttest). `AVAudioSession`
`.playAndRecord`/`.voiceChat`. Wire the same repository.
**Exit:** Phases 6–8 behaviour reproduced on an iOS device; Haze blur correct on
Skia/iOS; audio round-trip clean.
**Verify:** user (Mac + iOS device).

### Phase 10 — RevenueCat
**Scope:** RevenueCat KMP SDK init (both stores), `Billing` interface,
`Entitlements.POINT_PRO`. `PaywallViewModel` → offerings, `purchase`, `restore`,
`isPro`. `PlanToggle` prices from `StoreProduct.priceString`; `TrialCTA` starts
the purchase. Gate premium capability on `isPro`. Keep the custom `PremiumScreen`
UI unchanged.
**Exit:** sandbox purchase of monthly + yearly grants `point_pro`; restore works;
paywall shows live localized prices; gating enforced. Android + iOS.
**Verify:** user (sandbox accounts, both stores).

### Phase 11 — Polish: animation, haptics, liquid glass
**Scope:** Match every motion spec in `UI_SPEC.md` §4 (curves, durations, loops,
delays). Haptics on all interactive elements. Tune Haze (blur radius, tint,
noise, edge treatment) for a true "liquid glass" read in all lighting. Micro-
interactions: mic scale, button press, transition timing. Re-run the Phase 4
checklist as a full-app pass.
**Exit:** motion + glass indistinguishable from intent; Phase 4 checklist passes
app-wide; performance smooth (no jank on mid-range Android).
**Verify:** user.

### Post-Phase 11 — Retire RN
Move `app/`, `App.tsx`, `index.js`, Expo config, `node_modules` to `legacy-rn/`.
Update README. Remove after one stable KMP release on both stores.

---

## Parallelization

- Phase 4 corrections can overlap Phase 5.
- iOS design verification (subset of Phase 4) can start once Phase 3 builds on iOS.
- RevenueCat project + Firebase project setup (external, user-side) should start
  now so Phases 8 and 10 aren't blocked.
- "Liquid glass" tuning (Phase 11) can begin opportunistically during Phase 4.

## Risks & mitigations

| Risk | Mitigation |
|---|---|
| No design screenshots | Code tokens are exhaustive and used as SoT; request screenshots for Phase 4; treat any screenshot/code conflict as a Phase 4 decision |
| Haze fidelity ≠ `expo-blur` | Dedicated blur-tuning tasks in Phases 4 and 11; per-surface `HazeStyle`; fallback tint if a platform underperforms |
| Compose custom soft shadows on old Android | `RenderEffect` blur on API 31+, layered-alpha fallback below |
| Firebase AI Logic Live API maturity on KMP | Use the native platform SDKs (Android/Apple) behind `AiLiveSession`; keep a raw-WebSocket `@google/genai`-equivalent path as fallback if the Firebase live surface lags |
| iOS PCM plumbing (sample-rate conversion, session category) | Phase 7 loopback harness ported to iOS in Phase 9 before AI wiring |
| Session limits (audio+video ~2 min uncompressed, ~10 min socket) | Context compression + session resumption built into the repository in Phase 8 |
| Build toolchain not in this environment | All build/run verification delegated to user per phase; code authored to compile-clean by inspection + shared with user quickly |
| iOS requires a Mac | Phases 1–8 proceed Android-first; Phase 9 is explicitly Mac-gated |
| Scope of "pixel perfect" | Phase 4 discrepancy list with explicit sign-off / deferral, not open-ended polishing |

## Definition of done (project)

- All six screens pass `UI_SPEC.md` §5 on Android **and** iOS.
- Full camera + voice + vision loop works on both platforms via Gemini Live
  (Firebase AI Logic), including step/instruction/diagnosis updates.
- RevenueCat `point_pro` gating works in sandbox on both stores with the custom
  paywall.
- Motion + glass match `UI_SPEC.md` §4 / §1.7.
- RN implementation retired to `legacy-rn/`.
