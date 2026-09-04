package point.app.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

/** The six screens — mirrors the RN `RootStackParamList`. */
enum class PointDestination(val isModal: Boolean = false) {
    Splash,
    LiveCamera,
    LiveGuidance,
    StepTimeline,
    Diagnosis,
    Premium(isModal = true),
}

/**
 * A tiny back-stack navigator. Enough for Point's fixed set of screens; a
 * heavier library (navigation-compose) can replace this later without touching
 * the screens, which only depend on plain lambdas.
 */
class PointNavController(start: PointDestination) {
    private val stack: SnapshotStateList<PointDestination> = mutableStateListOf(start)

    val current: PointDestination get() = stack.last()
    val canPop: Boolean get() = stack.size > 1

    fun navigate(dest: PointDestination) {
        if (stack.lastOrNull() != dest) stack.add(dest)
    }

    /** Replace the whole stack — used for "restart" flows (e.g. finishing the paywall). */
    fun resetTo(dest: PointDestination) {
        stack.clear()
        stack.add(dest)
    }

    fun popBackStack(): Boolean {
        if (!canPop) return false
        stack.removeAt(stack.lastIndex)
        return true
    }
}

@Composable
fun rememberPointNavController(start: PointDestination = PointDestination.Splash): PointNavController =
    remember { PointNavController(start) }
