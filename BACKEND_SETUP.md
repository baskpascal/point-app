# Point — Backend setup (Firebase + RevenueCat)

Provisioned 2026-09-02 via the Firebase and RevenueCat MCP servers.
Account: `targaryenbaelor674@gmail.com` (Firebase) / RevenueCat OAuth session.

---

## Firebase — project `point-74ae6`

| | Value |
|---|---|
| Project ID | `point-74ae6` |
| Project number | `275457961562` |
| Storage bucket | `point-74ae6.firebasestorage.app` |
| Android app ID | `1:275457961562:android:517025478d6a37c7773263` (`com.point.repair`) |
| Android API key | `AIzaSyDQf0rottc3XV8wuhtUwx84TEHGiqUvJjI` |
| iOS app ID | `1:275457961562:ios:913c50fbd17d650b773263` (`com.point.repair`) |
| iOS API key | `AIzaSyC9yXTr_yMdp1MH8oIAXDraFr5IwDTaoVg` |
| Firebase AI Logic | **enabled** for both apps |

### Config files (staged in `firebase/`)
Once the KMP project is scaffolded, move them:

| File | Destination |
|---|---|
| `firebase/google-services.json` | `composeApp/google-services.json` |
| `firebase/GoogleService-Info.plist` | `iosApp/iosApp/GoogleService-Info.plist` |

### Still to do (needs the app running or store setup)
- [ ] **App Check** — register providers: Android **Play Integrity** (needs the
      debug + release SHA-256 from the signing keys — add via
      `firebase_create_android_sha` or the console), iOS **DeviceCheck/App Attest**.
- [ ] Accept "Gemini in Firebase" ToS **only if** you use the console AI assistant
      — not required for the Firebase AI Logic SDK path we use.
- [ ] Consider enabling Blaze (pay-as-you-go) before load testing — the Gemini
      Developer API path has a free tier that covers development.

### AI Logic usage from the app
The Firebase AI Logic SDK (`firebase-ai` Android / `FirebaseAI` Apple) talks to
Gemini through Firebase — **no raw Gemini API key in the binary**; App Check +
the Firebase config authorize the calls. Model for the live loop:
`gemini-3.1-flash-live-preview` (see `KMP_ARCHITECTURE.md` §5.3).

---

## RevenueCat — project `projf353231a`

| | Value |
|---|---|
| Project ID | `projf353231a` |
| Entitlement | `point_pro` (id `entl7fb9794acf`) |
| Offering | `default` (id `ofrng106fb8b2a8`) — **current** |
| Package — monthly | `$rc_monthly` (id `pkge6cb0fa7c1c`) |
| Package — yearly | `$rc_annual` (id `pkge090019e52e`) |
| Android app (Play Store) | id `app4acc073ced` — SDK key **`goog_qGxtsKFfxwrTYemstvQGRctIvCk`** |
| iOS app (App Store) | id `app919a74ddcd` — SDK key **`appl_YGYJiHfmRPBehJtLjEFfHLeClCN`** |

Init the RevenueCat KMP SDK with the `goog_…` key on Android and the `appl_…`
key on iOS (Phase 10). Entitlement check: `customerInfo.entitlements["point_pro"]`.

### Still to do (needs store products)
- [ ] Create subscription products in **Play Console** (e.g. `point_pro_monthly`
      @ $5.99/mo) and **App Store Connect** (e.g. `point_pro_yearly` @ $40.99/yr,
      plus a monthly), then **attach them to the packages** above
      (`$rc_monthly` → the monthly products, `$rc_annual` → the yearly products).
- [ ] **Play**: upload a Play service account JSON to the Android app
      (`play_service_account_credentials_configured: false`).
- [ ] **App Store**: add an App Store Connect API key + in-app-purchase key to the
      iOS app (`app_store_connect_api_key_configured: false`).
- [ ] Optional: build a RevenueCat paywall, or keep Point's custom `PremiumScreen`
      (the plan) and only pull `StoreProduct.priceString` for the price labels.

---

## What was NOT done

- **Running the app** — the KMP project is not scaffolded yet (pending the
  JetBrains wizard) and this environment has no JDK/Android SDK. The end-to-end
  test (Firebase AI Logic call + RevenueCat sandbox purchase) happens after
  Phase 1, on the Windows/Android-Studio machine.
- Store product creation and credential upload (above) — require Play Console /
  App Store Connect access.
