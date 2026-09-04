package point.app.platform

import androidx.compose.runtime.Composable

/**
 * Handle the platform "back" gesture (Android system back). iOS has no global
 * back gesture that maps to our custom navigator, so the actual there is a no-op.
 */
@Composable
expect fun BackNav(enabled: Boolean, onBack: () -> Unit)
