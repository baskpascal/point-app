# Point — UI Specification (migration contract)

This document is the **pixel-fidelity contract** for the Kotlin Multiplatform /
Compose Multiplatform rebuild. Every value here is extracted verbatim from the
existing React Native implementation under `app/` (the source of truth).

> **No screenshots were found in the repository.** The RN component + token code
> is therefore the authoritative reference. If you have the original design
> screenshots, drop them in `design/references/<screen>.png` so Phase 4 (visual
> comparison) can be done against images as well as code.

RN source files this spec is derived from:

| Area | Files |
|---|---|
| Tokens | `app/theme/{colors,gradients,radii,shadows,spacing,typography}.ts` |
| Foundation | `app/components/foundation/*` |
| Branding | `app/components/branding/*` |
| Screen components | `app/components/{camera,guidance,diagnosis,timeline,paywall}/*` |
| Screens | `app/screens/*` |
| Navigation | `app/navigation/RootNavigator.tsx` |

---

## 1. Design tokens

### 1.1 Color

| Token | Value | Notes |
|---|---|---|
| `bgPrimary` | `#F7F7F5` | warm off-white, base of every non-camera screen |
| `bgSecondary` | `#F2F3F5` | very light gray, bottom of background gradient |
| `surfaceGlass` | `rgba(255,255,255,0.42)` | light glass fill |
| `surfaceGlassStrong` | `rgba(255,255,255,0.58)` | light glass fill, cards |
| `strokeGlass` | `rgba(255,255,255,0.65)` | light glass inner border |
| `textPrimary` | `#101828` | |
| `textSecondary` | `#667085` | |
| `textTertiary` | `#98A2B3` | |
| `accentCoral` | `#FF6B6B` | primary accent, rings, highlights |
| `accentSalmon` | `#FF7A73` | |
| `accentOrange` | `#FF9E4A` | secondary accent, list bullets |
| `accentPeach` | `#FFD7C2` | soft fills (checks, active plan bg, thumb placeholder) |
| `successSoft` | `#78D38B` | "recognized" status dot |
| `warningSoft` | `#F9B35E` | |
| `overlayDark` | `rgba(16,24,40,0.35)` | |
| `white` | `#FFFFFF` | |
| `black` | `#000000` | camera screen root bg |

**Dark glass** (used over camera video — not a token in RN, but a consistent set):

| Usage | Fill | Border |
|---|---|---|
| Bubble (normal) | `rgba(16,24,40,0.30)` | `rgba(255,255,255,0.18)` |
| Bubble (emphasis) | `rgba(16,24,40,0.42)` | `rgba(255,255,255,0.18)` |
| Pill (dark tone) | `rgba(16,24,40,0.38)` | `rgba(255,255,255,0.18)` |
| Icon circle button (dark) | `rgba(16,24,40,0.35)` | `rgba(255,255,255,0.18)` |
| Voice dock / listening bar tint | `rgba(16,24,40,0.30)` | `rgba(255,255,255,0.18)` |
| Live Camera badge bg | `rgba(16,24,40,0.32)` | none |

### 1.2 Gradients

| Token | Stops | Direction (RN) |
|---|---|---|
| `cta` | `#FF5F7A` → `#FFA63D` | start (0,0) → end (1,1) — diagonal TL→BR |
| `ambientCoral` | `rgba(255,111,108,0.18)` → `rgba(255,111,108,0)` | radial-ish, low intensity |
| `ambientCool` | `rgba(140,160,200,0.14)` → `rgba(140,160,200,0)` | radial-ish, low intensity |
| `glassSheen` | `rgba(255,255,255,0.55)` → `rgba(255,255,255,0.12)` | top→bottom sheen |

App background actually renders (see `AppBackground.tsx`):
1. Linear `#F7F7F5` → `#F2F3F5` full-bleed.
2. Coral glow: `rgba(255,111,108,0.16)` → transparent, start (0,1) → end (0.6,0.4), layer opacity `0.9` (bottom-left).
3. Cool glow: `rgba(140,160,200,0.12)` → transparent, start (1,1) → end (0.4,0.4), layer opacity `0.9` (bottom-right).

### 1.3 Corner radii (dp)

| Token | Value |
|---|---|
| `pill` | 30 |
| `card` | 28 |
| `buttonLarge` | 32 |
| `sm` | 12 |
| `md` | 18 |

Other literal radii in components: node circles 34/2, mic button 26 (52px), icon circle button 22 (44px), timeline thumb 18, confidence ring stroke caps rounded, recognition-frame corner radius 12.

### 1.4 Spacing grid (dp)

`xs 4 · sm 8 · md 12 · lg 16 · xl 20 · xxl 24 · xxxl 32 · huge 40`

### 1.5 Typography

Font: **SF Pro Display / SF Pro Text** on iOS (system), **Inter** on Android
(bundle `Inter` TTFs — RN currently falls back to `sans-serif`, the Compose build
should ship Inter as a proper resource so Android matches iOS).

| Style | Size / line-height / weight | Usage |
|---|---|---|
| `heroTitle` | 46 / 52 / 700 | Confidence ring "%" |
| `screenTitle` | 32 / 38 / 700 | Wordmark, paywall headline |
| `bodyLarge` | 24 / 30 / 500 (700 where noted) | Diagnosis headline |
| `body` | 17 / 24 / 400 | Default body, bubbles, list items |
| `bodyMedium` | 17 / 24 / 600 | Button labels (rendered 700), active timeline label |
| `caption` | 13 / 18 / 500 (600–700 where noted) | Pills, badges, footnotes |

Letter-spacing: wordmark `-0.5`; diagnosis label `+0.6` uppercase; plan badge `fontSize 11`.

### 1.6 Shadows

| Token | iOS | Android |
|---|---|---|
| `soft` | color `#101828`, offset (0, 8), opacity `0.08`, radius `24` | elevation `4` |
| `glow` | color `#FF6B6B`, offset (0, 6), opacity `0.35`, radius `18` | elevation `6` |
| timeline active node | color `accentCoral`, offset (0, 4), opacity `0.45`, radius `12` | — |

Compose: implement with a soft ambient shadow (custom `Modifier.dropShadow` /
`graphicsLayer` + blurred layer, or `Modifier.shadow` with large `ambientColor` /
`spotColor` and generous `blurRadius`) — **not** default Material elevation
overlays.

### 1.7 Blur (glass)

RN uses `expo-blur` `BlurView` with `intensity` 0–100 and `tint` `light`/`dark`.
Map to **Haze** `HazeStyle` (`blurRadius` in dp, `tint`, `noiseFactor`). Starting
conversion (tune visually in Phase 4/11):

| Surface | RN intensity / tint | Haze start point |
|---|---|---|
| `GlassCard` | 24 / light | blurRadius ~20dp, tint = `surfaceGlass(Strong)` |
| `GlassPill` default | 22 / light | ~18dp, tint `surfaceGlass` |
| `GlassPill` dark | 32 / dark | ~24dp, tint `rgba(16,24,40,0.38)` |
| `GlassBubble` normal | 22 / dark | ~18dp, tint `rgba(16,24,40,0.30)` |
| `GlassBubble` emphasis | 30 / dark | ~24dp, tint `rgba(16,24,40,0.42)` |
| `IconCircleButton` light / dark | 20 / 30 | ~16dp / ~24dp |
| `VoiceInputDock` | 26 / dark | ~22dp, tint `rgba(16,24,40,0.30)` |
| Listening bar | 26 / dark | ~22dp |

All glass surfaces: `overflow: hidden` + rounded clip, **1dp inner border**
(light: `strokeGlass`; dark: `rgba(255,255,255,0.18)`), never fully opaque
("milky" is a bug).

---

## 2. Foundation components

### 2.1 `AppBackground`
Full-screen. Base gradient + two ambient glows (§1.2). Children on top. Root bg `bgPrimary`.

### 2.2 `GlassCard`
- Default radius `28` (`card`), `padding: 20` on content.
- `intensity` default 24; `strong` swaps fill `surfaceGlass` → `surfaceGlassStrong`.
- Wrapper carries `soft` shadow, `overflow: hidden`.
- Layers: blur → fill+border overlay → content.

### 2.3 `GlassPill`
- `pill` radius (30), `alignSelf: flex-start`.
- Row: `paddingVertical 8`, `paddingHorizontal 16`, `gap 4`, center-aligned.
- Label `caption` weight 600. `default` tone → light glass + `textPrimary`; `dark` tone → dark glass + `white`.
- Optional leading `icon` slot.

### 2.4 `GlassBubble`
- `md` radius (18), `maxWidth 86%`, `paddingVertical 12`, `paddingHorizontal 16`.
- `align` `left`/`right` → `alignSelf`. `emphasis` → stronger fill + intensity 30.
- Text `body` / `white`. No tails, no solid color blocks.

### 2.5 `GradientButton`
- `buttonLarge` radius (32). `cta` gradient, start (0,0) → end (1,1).
- `large`: `paddingVertical 18` (`lg`+2), `paddingHorizontal 32`. `medium`: `paddingVertical 12`, `paddingHorizontal 24`.
- Row, centered, `gap 8`. Label `bodyMedium` rendered **weight 700**, `white`.
- `loading` → centered spinner (white). `disabled` → `opacity 0.5`.
- **Haptic:** medium impact on press (before `onPress`).

### 2.6 `IconCircleButton`
- Size default `44`, fully round. `overflow: hidden`, centered child.
- `dark` tone (default): dark glass fill + border. `light` tone: light glass.
- `activeOpacity 0.75`.
- Used for close (× = 14×2dp white/dark bar rotated) and back (10×10dp chevron: 2dp left+bottom borders, rotate 45°).

### 2.7 Branding
- `PointLogo`: `cta`-gradient ring (full circle), centered white inner dot at `0.42 × size`, inner radius half of that. Default size 72.
- `BrandLockup`: vertical stack `gap 12` → `PointLogo` (size prop) → `"Point"` (`screenTitle`, `textPrimary`, letter-spacing −0.5) → optional tagline (`body`, `textSecondary`). Default tagline `"Point. Ask. Fix."`.

---

## 3. Screens

Coordinate note: RN `SafeAreaView` → Compose `WindowInsets.safeDrawing`. Screen
horizontal padding is per-screen (below). Stack transition = **fade** (all
routes); `Premium` is a **modal** presentation (slide up from bottom).

### 3.1 Splash / Start (`SplashScreen.tsx`)
- `AppBackground`.
- Centered `BrandLockup` (logoSize `84`, tagline `"Point. Ask. Fix."`), vertically + horizontally centered (`flex: 1`, center).
- Bottom CTA area: `paddingHorizontal 32` (`xxxl`), `paddingBottom 40` (`huge`), centered. `GradientButton` label **"Start a repair"** → navigates to `LiveCamera`.

### 3.2 Live Camera (`LiveCameraScreen.tsx`)
Root bg `#000`. Full-bleed `CameraPreview` (fallback: solid `#12151A` with centered `"Point your camera at the tool"` `body`/white/opacity 0.6). Overlay is a safe-area column, `paddingHorizontal 16`, `paddingBottom 16`, `justifyContent: space-between`:
- **Top row** (`marginTop 8`, space-between, center):
  - `IconCircleButton` (dark, close ×) → `goBack`.
  - `ObjectRecognizedPill` — `GlassPill` dark, leading 8dp `successSoft` dot (`marginRight 6`), label e.g. `"Makita DHP485 recognized"`.
  - 44dp spacer (keeps pill centered).
- **Center** (`flex: 1`, centered): `RecognitionFrame` — 220×220, 4 animated corner brackets, `CORNER` 28dp, 3dp borders, corner radius 12, color `accentPeach`. Pulse: opacity 0.6↔1, 1400ms, `easeInOut`, repeat-reverse.
- **Bottom**:
  - `LiveCameraBadge` — row, dark bg `rgba(16,24,40,0.32)`, `pill` radius, `paddingV 6 / paddingH 12`, `gap 4`. 6dp `#FF5A5A` dot pulsing opacity 1↔0.4 @ 700ms repeat-reverse + `"Live Camera"` (`caption`/600/white).
  - `spacing.md` gap.
  - `VoiceInputDock` — `buttonLarge` radius, blur 26 dark + tint, row space-between, `paddingV 12 / paddingH 20`. Left: prompt `"What do you want to fix?"` (`body`/white, flex 1, `marginRight 12`). Right: 52dp `cta`-gradient mic button (round, 16dp white dot icon; `listening` → `scale 1.05`). **Haptic** medium on press. Press → `setListening(!listening)` then navigate to `LiveGuidance`.

### 3.3 Live Guidance (`LiveGuidanceScreen.tsx`) — CRITICAL
**No large opaque white panel.** Camera stays visible. Root bg `#000`, full-bleed `CameraPreview`, overlay column `paddingHorizontal 16 / paddingBottom 16`, `space-between`:
- **Top row** (`marginTop 8`, space-between, center):
  - `IconCircleButton` (dark, back chevron) → `goBack`.
  - `StepPill` — `GlassPill` dark, label `"Step {current} of {total}"` (1-indexed).
  - 44dp tappable spacer → navigates to `StepTimeline` (invisible hit target).
- **Center** (`flex: 1`, centered): `HighlightOverlay` — 160dp, coral ring only (`borderWidth 3`, `borderColor accentCoral`, `borderRadius 999`). Glow: opacity 0.4↔1 @ 1100ms repeat-reverse. Never a filled block.
- **Conversation** (`marginBottom 16`):
  - `InstructionBubble` — `GlassBubble` emphasis, left, `maxWidth 92%`. Text e.g. `"Disconnect the black cable on the left."`
  - `spacing.sm` gap, then each `ConversationBubble` (`marginTop 8`): user → right align, assistant → left align. All are dark glass capsules (`GlassBubble`), `maxWidth 86%`.
- **Listening bar** (`alignSelf: center`, `borderRadius 24`, `overflow hidden`, `paddingV 8 / paddingH 20`): blur 26 dark + `rgba(16,24,40,0.30)` tint + `rgba(255,255,255,0.18)` 1dp border → `ListeningWave` size `sm`.

### 3.4 Step Timeline (`StepTimelineScreen.tsx`)
- `AppBackground` + safe area. Top row `paddingHorizontal 16 / paddingTop 8`: `IconCircleButton` light (back).
- Scroll content `paddingHorizontal 20 / paddingTop 20 / paddingBottom 32`:
  - `RepairHeaderCard` — `GlassCard` strong, `padding 0`, inner row `padding 16 / gap 16`, center. 56dp thumb (`borderRadius 18`, `overflow hidden`; placeholder = `accentPeach` fill). Right: title (`bodyMedium`/`textPrimary`), subtitle (`caption`/`textSecondary`, `marginTop 2`), e.g. `"Makita DHP485"` / `"Repair in progress"`.
  - `spacing.xxxl` gap.
  - `TimelineList` — per step, a rail column (width 34) + label. Node 34dp circle:
    - `completed`: `textPrimary` fill, white `✓` (700).
    - `active`: `cta` gradient fill, white index (700), coral shadow (opacity 0.45 / radius 12 / offset (0,4)).
    - `pending`: `rgba(16,24,40,0.06)` fill, `textTertiary` index (600).
    - Connector: 2dp vertical, `minHeight 24`, `rgba(16,24,40,0.08)`; filled (`textPrimary`) when the step above is completed.
    - Label column: `paddingLeft 16 / paddingBottom 24 / paddingTop 4`. `active` → `bodyMedium`/`textPrimary`; `pending` → `textTertiary`; else `body`/`textPrimary`.
- Footer `paddingHorizontal 20 / paddingBottom 20`: `TimelineCTA` = full-width `GradientButton`, label `"Continue step {activeIndex+1}"` → `LiveGuidance`.

### 3.5 Diagnosis (`DiagnosisScreen.tsx`)
- `AppBackground` + safe area. Top row `paddingHorizontal 16 / paddingTop 8`: `IconCircleButton` light (back).
- Scroll content `paddingHorizontal 20 / paddingTop 20 / paddingBottom 32`, center-aligned:
  - `ConfidenceRing` — 200dp, stroke 14dp. Track `rgba(16,24,40,0.06)`; progress arc `accentCoral`, round caps, starts at −90° (12 o'clock). Center `"{percent}%"` `heroTitle`/`textPrimary`. Animate 0→percent, 900ms, `easeOutCubic`. `marginTop 16`.
  - `spacing.xxxl` gap.
  - `DiagnosisInfoCard` — `GlassCard` strong, full width. Label (`caption`/700/`accentCoral`/uppercase/ls +0.6), headline (`bodyLarge`/700/`textPrimary`, `marginTop 8`), supporting (`body`/`textSecondary`, `marginTop 4`). E.g. `DIAGNOSIS` / `"Worn chuck assembly"` / `"The chuck no longer grips reliably."`
  - `spacing.xxxl` gap.
  - `NextActionSection` — title `"What to do next"` (`bodyMedium`/`textPrimary`), then rows: 6dp `accentOrange` bullet (`marginTop 8`, top-aligned) + item (`body`/`textSecondary`). Row `gap 8`, section `gap 8`.
- Footer `paddingHorizontal 20 / paddingBottom 20`, centered: `GradientButton` `"Guide me"` → `LiveGuidance`.

### 3.6 Premium / Paywall (`PremiumScreen.tsx`) — modal
- `AppBackground` + safe area. Top row `paddingHorizontal 16 / paddingTop 8`, space-between: 44dp spacer + `IconCircleButton` light (close ×) → `goBack`.
- Scroll content `paddingHorizontal 20 / paddingBottom 32`, center-aligned:
  - `BrandLockup` (tagline `""`, logoSize `56`).
  - `spacing.xl` gap.
  - Headline `"Fix with confidence."` (`screenTitle`/`textPrimary`, centered).
  - Subheadline `"Go beyond the basics with premium tools that help you repair smarter."` (`body`/`textSecondary`, centered, `marginTop 8`, `paddingHorizontal 16`).
  - `spacing.xxxl` gap.
  - `PremiumBenefitsCard` — `GlassCard` strong. Rows (`gap 12`, `marginTop 12` between): 26dp `accentPeach` round check with `accentCoral` `✓` (800/13) + benefit (`body`/`textPrimary`). Benefits: `"Unlimited live guidance"`, `"Repair history"`, `"Works across your tools"`.
  - `spacing.xxl` gap.
  - `PlanToggle` — row `gap 12`, full width. Each option: `md` radius, `borderWidth 1.5`, border `rgba(16,24,40,0.08)`, bg `white`, `paddingV 16 / paddingH 12`, center. Active → border `accentCoral`, bg `accentPeach`. `yearly` has a floating badge (top −10, `accentCoral` bg, `pill` radius, `paddingH 8 / paddingV 2`, `"Save 40%"` `caption`/700/white/11). Labels: `Monthly` `$5.99/mo`; `Yearly` `$40.99/yr`. *(Prices become RevenueCat `StoreProduct.priceString` in Phase 10.)*
- Footer `paddingHorizontal 20 / paddingBottom 20`: `TrialCTA` — full-width `GradientButton` `"Start free trial"` + footer `"7 days free. Cancel anytime."` (`caption`/`textTertiary`), `gap 10`, centered.

---

## 4. Motion spec

| Element | Property | Curve / duration | Loop |
|---|---|---|---|
| Stack transitions | opacity | fade, platform default (~300ms) | — |
| `Premium` route | translateY | modal slide-up | — |
| `RecognitionFrame` corners | opacity 0.6↔1.0 | 1400ms, easeInOut | infinite reverse |
| `HighlightOverlay` ring | opacity 0.4↔1.0 | 1100ms, linear timing | infinite reverse |
| `LiveCameraBadge` dot | opacity 1.0↔0.4 | 700ms timing | infinite reverse |
| `ListeningWave` bar _i_ | scaleY 0.3↔1.0 (`sm` maxHeight 14, `md` 22) | 320ms up + 320ms down, start delay `i·90ms` | infinite reverse |
| `ListeningWave` inactive | scaleY → 0.3 | single timing | — |
| `ConfidenceRing` arc | strokeDashoffset (0→percent) | 900ms, easeOutCubic | once, on mount / percent change |
| Mic button (`listening`) | scale → 1.05 | immediate (spring recommended) | — |
| `GradientButton` / mic press | haptic | medium impact | on press |

Bars: 5, width 4dp, `gap 4dp`, bottom-anchored, `cta` vertical gradient, radius 2.

---

## 5. Fidelity checklist (per screen, Phase 4 gate)

For each of the six screens, verify against RN render **and** screenshots (when
provided):

- [ ] Background: correct base gradient + both ambient glows, positions/opacity
- [ ] Horizontal & vertical padding matches the per-screen values in §3
- [ ] Type: family, size, line-height, weight, letter-spacing, color per token
- [ ] Baseline alignment of stacked text blocks
- [ ] Glass: fill opacity, blur radius, 1dp inner border, corner radius, clip
- [ ] Shadows: soft ambient only, no Material elevation tint
- [ ] Gradients: exact stops + direction (cta is TL→BR diagonal)
- [ ] Icon sizes & 44dp minimum touch targets
- [ ] Accent usage: coral for rings/labels, orange for bullets, peach for soft fills
- [ ] Animation curve + duration + loop behaviour per §4
- [ ] Live Guidance: camera visible, zero opaque panels, elements float
- [ ] Safe-area insets respected top and bottom
