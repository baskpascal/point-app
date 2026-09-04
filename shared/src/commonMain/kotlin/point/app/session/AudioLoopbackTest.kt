package point.app.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import point.app.design.PointTheme
import point.app.ui.foundation.PText

/**
 * Phase 7 verification widget — capture ~3 s from the mic and play it straight
 * back. Proves the AudioRecord → AudioTrack pipeline and the PCM formats.
 * Remove once the Gemini Live session (Phase 8) drives the engine.
 */
@Composable
fun AudioLoopbackTest(modifier: Modifier = Modifier) {
    val engine = rememberLiveAudioEngine()
    val perm by engine.permission.collectAsState()
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("tap: 3 s mic loopback") }
    var busy by remember { mutableStateOf(false) }

    Column(
        modifier
            .background(PointTheme.colors.glass72, RoundedCornerShape(14.dp))
            .clickable(enabled = !busy) {
                if (perm != AudioPermission.GRANTED) {
                    engine.requestPermission()
                    status = "requesting mic permission…"
                    return@clickable
                }
                busy = true
                scope.launch {
                    status = "recording 3 s…"
                    val captured = ArrayList<PcmChunk>()
                    val job = launch { engine.startCapture().collect { captured.add(it) } }
                    delay(3_000)
                    job.cancel()
                    engine.stopCapture()
                    val total = captured.sumOf { it.bytes.size }
                    val rate = captured.firstOrNull()?.sampleRate ?: 0
                    status = "playing back $total bytes @ ${rate}Hz"
                    captured.forEach { engine.play(it) }
                    delay(3_500)
                    status = "done — $total bytes captured @ ${rate}Hz"
                    busy = false
                }
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PText("🎤 audio loopback (dev)", PointTheme.type.micro, PointTheme.colors.ink)
        PText(status, PointTheme.type.micro, PointTheme.colors.slate)
    }
}
