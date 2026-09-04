package point.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import point.app.design.PointTheme
import point.app.screen.PointApp

@Composable
@Preview
fun App() {
    PointTheme {
        PointApp()
    }
}
