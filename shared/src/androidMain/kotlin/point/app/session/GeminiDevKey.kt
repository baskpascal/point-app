package point.app.session

/**
 * DEV-ONLY. Holds the direct Gemini API key for [DirectGeminiLiveSession], the
 * temporary development transport for the Live loop. The value is injected once
 * from `PointApplication` (sourced from `local.properties`, never committed).
 *
 * Production uses the Firebase AI Logic path ([FirebaseAiLiveSession]) and this
 * holder stays empty.
 */
object GeminiDevKey {
    @Volatile
    var value: String = ""
}
