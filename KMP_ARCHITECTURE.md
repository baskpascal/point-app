# Point — Kotlin Multiplatform Architecture

Target: **Android + iOS**, one shared UI and product core in `commonMain`,
platform code confined to `androidMain` / `iosMain`.

---

## 1. Toolchain

| Tool | Version (pin in Phase 1) | Notes |
|---|---|---|
| Kotlin | 2.1.x | K2 |
| Compose Multiplatform | 1.7.x+ (JetBrains) | Compose 1.8 material3 ok, but **no Material styling** — use `Surface`/`Text`/`Box` primitives + custom theme |
| Gradle | 8.9+ | |
| Android Gradle Plugin | 8.7+ | `minSdk 26`, `compileSdk/targetSdk 35` |
| Xcode | 15+ | iOS deployment target 15.0 |
| JDK | 17 | |

> **This WSL environment has no JDK / Android SDK / Xcode.** Code is authored
> here; **builds and device runs happen on the user's Windows machine (Android
> Studio) and a Mac (iOS).** Every "verify build" step in the plan is executed
> there, not here.

---

## 2. Module & package layout

Single Gradle module `:composeApp` (simplest structure for Shipaton; split into
`:core:*` / `:feature:*` later only if build times demand it).

```
point-app/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml
├── composeApp/
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/app/point/
│       │   ├── PointApp.kt                 # root @Composable, theme + nav host
│       │   ├── design/
│       │   │   ├── PointTheme.kt           # CompositionLocals, PointTheme { }
│       │   │   ├── PointColors.kt          # §1.1 of UI_SPEC
│       │   │   ├── PointTypography.kt      # §1.5
│       │   │   ├── PointSpacing.kt         # §1.4
│       │   │   ├── PointRadii.kt           # §1.3
│       │   │   ├── PointElevation.kt       # soft/glow shadow modifiers (§1.6)
│       │   │   ├── PointGradients.kt       # §1.2 (Brush factories)
│       │   │   └── glass/PointGlass.kt     # Haze styles per surface (§1.7)
│       │   ├── ui/
│       │   │   ├── foundation/             # AppBackground, GlassCard, GlassPill,
│       │   │   │                           #   GlassBubble, GradientButton, IconCircleButton
│       │   │   ├── branding/               # PointLogo, BrandLockup
│       │   │   ├── camera/                 # RecognitionFrame, LiveCameraBadge,
│       │   │   │                           #   ObjectRecognizedPill, VoiceInputDock, ListeningWave
│       │   │   ├── guidance/               # StepPill, HighlightOverlay, InstructionBubble, ConversationBubble
│       │   │   ├── diagnosis/              # ConfidenceRing, DiagnosisInfoCard, NextActionSection
│       │   │   ├── timeline/               # RepairHeaderCard, TimelineList, TimelineCTA
│       │   │   └── paywall/                # PlanToggle, PremiumBenefitsCard, TrialCTA
│       │   ├── screen/
│       │   │   ├── SplashScreen.kt
│       │   │   ├── LiveCameraScreen.kt
│       │   │   ├── LiveGuidanceScreen.kt
│       │   │   ├── StepTimelineScreen.kt
│       │   │   ├── DiagnosisScreen.kt
│       │   │   └── PremiumScreen.kt
│       │   ├── nav/
│       │   │   ├── PointDestination.kt     # sealed routes (mirrors RootStackParamList)
│       │   │   └── PointNavHost.kt         # NavHost, fade transitions, Premium = modal
│       │   ├── state/
│       │   │   ├── model/                  # RepairStep, ToolRecognition, Diagnosis,
│       │   │   │                           #   ConversationMessage, PlanId, Entitlement
│       │   │   ├── RepairSessionState.kt   # immutable UI state
│       │   │   ├── RepairSessionStore.kt   # StateFlow<RepairSessionState> + intents
│       │   │   └── PaywallViewModel.kt
│       │   ├── session/
│       │   │   ├── CameraController.kt     # interface (expect-free)
│       │   │   ├── LiveAudioEngine.kt      # interface
│       │   │   ├── AiLiveSession.kt        # interface + AiEvent sealed hierarchy
│       │   │   ├── RepairSessionRepository.kt   # interface
│       │   │   └── DefaultRepairSessionRepository.kt  # orchestration (common)
│       │   ├── billing/
│       │   │   ├── Billing.kt              # interface over RevenueCat KMP
│       │   │   └── Entitlements.kt         # "point_pro"
│       │   ├── platform/
│       │   │   ├── CameraPreview.kt        # expect @Composable
│       │   │   └── Platform.kt             # expect (name, haptics)
│       │   ├── mock/
│       │   │   └── MockRepairSession.kt    # port of app/mocks/repairSession.ts
│       │   └── di/
│       │       └── Modules.kt              # Koin common module
│       ├── commonMain/composeResources/    # Inter fonts, icons, Lottie/none
│       ├── androidMain/kotlin/app/point/
│       │   ├── MainActivity.kt
│       │   ├── PointApplication.kt         # Firebase + App Check init
│       │   ├── platform/CameraPreview.android.kt   # AndroidView(PreviewView)
│       │   ├── session/CameraXController.kt
│       │   ├── session/AndroidLiveAudioEngine.kt   # AudioRecord + AudioTrack
│       │   ├── session/FirebaseAiLiveSession.kt    # Firebase AI Logic Android SDK
│       │   └── di/AndroidModule.kt
│       ├── androidMain/AndroidManifest.xml         # CAMERA, RECORD_AUDIO
│       └── iosMain/kotlin/app/point/
│           ├── MainViewController.kt
│           ├── platform/CameraPreview.ios.kt       # UIKitView(AVCaptureVideoPreviewLayer)
│           ├── session/AVFoundationCameraController.kt
│           ├── session/IosLiveAudioEngine.kt       # AVAudioEngine / AVAudioPlayerNode
│           ├── session/FirebaseAiLiveSession.kt    # Firebase AI Logic Apple SDK
│           └── di/IosModule.kt
└── iosApp/                                          # Xcode project, SPM: FirebaseAI, FirebaseAppCheck, RevenueCat
```

---

## 3. Design system in `commonMain`

- `PointTheme { content }` installs `CompositionLocal`s: `LocalPointColors`,
  `LocalPointTypography`, `LocalPointSpacing`, `LocalPointRadii`,
  `LocalPointGradients`, `LocalPointGlass`. Accessor object `PointTheme.colors`
  etc. (Material-theme pattern, Point values).
- **No `MaterialTheme`.** If a Compose component needs it (ripple), wrap locally
  with a neutral theme or disable ripple via `LocalIndication`.
- Typography ships **Inter** (`composeResources/font/`) so Android == iOS; iOS may
  additionally prefer the system SF Pro — decide in Phase 4.
- Shadows: `Modifier.softShadow()` / `Modifier.glowShadow()` implemented with a
  blurred offset layer (`graphicsLayer` + `RenderEffect` on Android 12+/Skia on
  iOS) — see §1.6 of UI_SPEC. Provide a graceful fallback (elevation-like) for
  Android < 12.
- Gradients: `PointGradients.cta = Brush.linearGradient(listOf(FF5F7A, FFA63D))`
  with `start = Offset.Zero`, `end = Offset.Infinite` (TL→BR). Ambient glows as
  `Brush.radialGradient`.

### 3.1 Glass — Haze

- Dependency: `dev.chrisbanes.haze:haze` (+ `haze-materials` optional).
- Camera screens: the `CameraPreview` composable is the `hazeSource`
  (`Modifier.hazeSource(state)`); every overlay glass surface uses
  `Modifier.hazeEffect(state, style = PointGlass.<surface>)`.
- Light screens (`AppBackground`): background is `hazeSource`; `GlassCard` etc.
  use `hazeEffect`. Since the light background is nearly flat, a subtle blur +
  the white tint carries the effect.
- `HazeStyle(blurRadius = Xdp, backgroundColor = ..., tint = HazeTint(color),
  noiseFactor = 0.03f)` — starting values in UI_SPEC §1.7.
- Every glass surface still draws its **1dp inner border** and rounded clip
  explicitly on top of the haze effect.

---

## 4. State & navigation (`commonMain`)

### 4.1 Models (port of `app/mocks/repairSession.ts` + `store/repairStore.ts`)

```kotlin
data class RepairStep(val id: String, val label: String)
enum class StepStatus { COMPLETED, ACTIVE, PENDING }
data class ToolRecognition(val name: String, val recognizedLabel: String, val thumbnailUrl: String?)
data class ConversationMessage(val role: Role, val text: String) // Role: USER, ASSISTANT
data class Diagnosis(
    val confidence: Int, val label: String, val headline: String,
    val supportingText: String, val nextActions: List<String>,
)
enum class PlanId { MONTHLY, YEARLY }

data class RepairSessionState(
    val tool: ToolRecognition?,
    val steps: List<RepairStep>,
    val activeStepIndex: Int,
    val instruction: String?,
    val conversation: List<ConversationMessage>,
    val diagnosis: Diagnosis?,
    val isListening: Boolean,
    val selectedPlan: PlanId,
    val connection: ConnectionState, // IDLE, CONNECTING, LIVE, RECONNECTING, ERROR
)
```

### 4.2 `RepairSessionStore`
- Holds `MutableStateFlow<RepairSessionState>`, seeded from `MockRepairSession`
  (Phase 3) then driven by `RepairSessionRepository` (Phase 5+).
- Intents: `setActiveStepIndex`, `advanceStep`, `setListening`, `selectPlan`,
  `applyAiUpdate(RepairStatePatch)`, `appendMessage`.
- Survives config changes via `viewModel { }` (Compose MP `lifecycle-viewmodel`)
  or a Koin single scoped to the nav graph.

### 4.3 Navigation
- `org.jetbrains.androidx.navigation:navigation-compose` (Compose MP nav).
- `PointDestination`: `Splash`, `LiveCamera`, `LiveGuidance`, `StepTimeline`,
  `Diagnosis`, `Premium` — 1:1 with `RootStackParamList`.
- `NavHost`: default `enterTransition/exitTransition = fadeIn()/fadeOut()`;
  `Premium` composable uses a vertical slide (`slideInVertically`) to emulate the
  RN `presentation: 'modal'`.
- Start destination `Splash`.

---

## 5. Platform abstractions

All interfaces live in `commonMain`; **no `expect`/`actual` on Firebase or media
types** — only on the Compose `CameraPreview` view and trivial platform helpers.

### 5.1 `CameraController`
```kotlin
interface CameraController {
    val state: StateFlow<CameraState>            // permission, running, error
    suspend fun start(lens: Lens = Lens.BACK)
    fun stop()
    /** ~1 fps downscaled JPEG frames for the AI session. */
    val frames: Flow<CameraFrame>                // CameraFrame(jpeg: ByteArray, width, height, ts)
    suspend fun requestPermission(): Boolean
}
@Composable expect fun CameraPreview(controller: CameraController, modifier: Modifier)
```
- Android: CameraX `ProcessCameraProvider`, `Preview` + `ImageAnalysis`
  (`STRATEGY_KEEP_ONLY_LATEST`, throttled to ~1 fps, YUV→JPEG). Preview via
  `AndroidView(PreviewView)`.
- iOS: `AVCaptureSession` + `AVCaptureVideoDataOutput` (frame throttle), preview
  via `UIKitView` wrapping `AVCaptureVideoPreviewLayer`.

### 5.2 `LiveAudioEngine`
```kotlin
interface LiveAudioEngine {
    /** 16-bit mono PCM at 16 kHz, ~20–40 ms chunks. */
    fun startCapture(): Flow<PcmChunk>
    fun stopCapture()
    /** Enqueue 16-bit mono PCM at 24 kHz for playback. */
    suspend fun play(chunk: PcmChunk)
    fun clearPlayback()            // on AiEvent.Interrupted
    fun release()
}
```
- Android: `AudioRecord` (`MIC`, 16000, `CHANNEL_IN_MONO`, `ENCODING_PCM_16BIT`)
  for capture; `AudioTrack` (`MODE_STREAM`, 24000) for playback. AEC via
  `AcousticEchoCanceler` when available.
- iOS: `AVAudioEngine` input tap (convert to 16 kHz via `AVAudioConverter`);
  `AVAudioPlayerNode` scheduling 24 kHz buffers. `AVAudioSession`
  `.playAndRecord` + `.voiceChat` mode.

### 5.3 `AiLiveSession` (Gemini Live via Firebase AI Logic)
```kotlin
interface AiLiveSession {
    suspend fun connect(config: LiveConfig)   // model gemini-3.1-flash-live-preview
    suspend fun sendAudio(chunk: PcmChunk)    // audio/pcm;rate=16000
    suspend fun sendVideoFrame(frame: CameraFrame) // image/jpeg
    val events: Flow<AiEvent>
    suspend fun close()
}
sealed interface AiEvent {
    data class InputTranscript(val text: String, val isFinal: Boolean) : AiEvent
    data class OutputTranscript(val text: String) : AiEvent
    data class AudioOut(val chunk: PcmChunk) : AiEvent            // 24 kHz PCM
    data class RepairStateUpdate(val patch: RepairStatePatch) : AiEvent // from set_repair_state function call
    data object Interrupted : AiEvent
    data class Error(val cause: Throwable) : AiEvent
    data object GoAway : AiEvent                                  // trigger session resumption
}
```
- Android: **Firebase AI Logic Android SDK** (`com.google.firebase:firebase-ai`),
  `LiveGenerativeModel` / live session API, model
  `gemini-3.1-flash-live-preview`, `responseModalities = [AUDIO]`,
  `inputAudioTranscription` + `outputAudioTranscription` enabled, a `Tool`
  function declaration `set_repair_state`.
- iOS: **Firebase AI Logic Apple SDK** (`FirebaseAI`), same model + config.
- **Structured UI updates** = one synchronous function tool `set_repair_state`
  with params `{ currentStepIndex, steps[], instruction, diagnosis?, toolName? }`.
  On call → emit `RepairStateUpdate` → `RepairSessionStore.applyAiUpdate` → send
  an empty function response back (sync FC requires it).
- Session limits handled in `DefaultRepairSessionRepository`: context-window
  compression on; on `GoAway` / socket close, reconnect with the resumption
  handle and replay minimal state.

### 5.4 `RepairSessionRepository`
```kotlin
interface RepairSessionRepository {
    val state: StateFlow<RepairSessionState>
    suspend fun startLiveSession()   // wires Camera.frames + Audio.capture → AiLiveSession; AiEvent → store + Audio.play
    fun stopLiveSession()
    fun sendUserText(text: String)   // typed-question fallback
}
```
Pure `commonMain` orchestration over the three interfaces + the store. Coroutine
scope tied to the Live Camera / Live Guidance nav scope.

---

## 6. Firebase

| | Android | iOS |
|---|---|---|
| SDK | Firebase BoM + `firebase-ai` + `firebase-appcheck-playintegrity` | SPM: `FirebaseAI`, `FirebaseAppCheck` (`DeviceCheck`/`AppAttest`) |
| Config | `google-services.json` + `com.google.gms.google-services` plugin | `GoogleService-Info.plist` |
| Init | `PointApplication.onCreate` → `FirebaseApp.initializeApp` → `FirebaseAppCheck.installAppCheckProviderFactory(...)` | `AppDelegate` / `@main` → `FirebaseApp.configure()` → App Check provider |

App Check gates the Gemini Live calls so no API key ships in the binary — this is
the "minimal auth service" requirement, satisfied by Firebase.

---

## 7. RevenueCat (Phase 10)

- Official **RevenueCat Kotlin Multiplatform SDK**
  (`com.revenuecat.purchases:purchases-kmp-core` + UI packages as needed).
- Init in platform code with per-store API keys; shared `Billing` interface in
  `commonMain`.
- Entitlement id: **`point_pro`**. Offerings: packages `monthly`, `yearly`.
- `PaywallViewModel` exposes `offerings`, `selectedPlan`, `purchase()`,
  `restore()`, `isPro: StateFlow<Boolean>` (from
  `CustomerInfo.entitlements["point_pro"]?.isActive`).
- **Keep the custom `PremiumScreen`** — RevenueCat only provides data + purchase
  calls, not UI. `PlanToggle` prices come from `StoreProduct.priceString`.
- Gate: live guidance / unlimited sessions require `isPro`; free tier = limited.

---

## 8. Dependency injection

Koin (`io.insert-koin:koin-core` + `koin-compose`). `commonModule` provides
store, repository, mock data, view models. `androidModule` / `iosModule` provide
`CameraController`, `LiveAudioEngine`, `AiLiveSession`, `Billing`. Started from
`MainActivity` / `MainViewController`.

---

## 9. `libs.versions.toml` (fill exact versions in Phase 1)

```
kotlin, agp, compose-multiplatform, androidx-lifecycle (viewmodel + runtime-compose),
navigation-compose (JB), kotlinx-coroutines, kotlinx-serialization, kotlinx-datetime,
haze, koin, firebase-bom, firebase-ai, firebase-appcheck, revenuecat-kmp,
camerax (androidMain), accompanist-permissions? (no — custom), okio
```

---

## 10. What stays out of `commonMain`

- Any `com.google.firebase.*` / `FirebaseAI` / `AVFoundation` / `androidx.camera.*`
  / `android.media.*` import.
- `google-services.json`, `GoogleService-Info.plist`.
- Store API keys.
- The Xcode project (`iosApp/`) and Gradle Android config.
