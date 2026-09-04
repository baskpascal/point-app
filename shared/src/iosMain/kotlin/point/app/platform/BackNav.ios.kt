package point.app.platform

import androidx.compose.runtime.Composable

@Composable
actual fun BackNav(enabled: Boolean, onBack: () -> Unit) {
    // iOS: no global back gesture bound to the custom navigator (yet).
}
