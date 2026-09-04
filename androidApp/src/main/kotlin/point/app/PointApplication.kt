package point.app

import android.app.Application
import com.google.firebase.FirebaseApp
import point.app.session.GeminiDevKey

class PointApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        installAppCheck()
        // DEV-ONLY: hand the direct Gemini Live transport its key (see DirectGeminiLiveSession).
        GeminiDevKey.value = BuildConfig.GEMINI_API_KEY
    }
}
