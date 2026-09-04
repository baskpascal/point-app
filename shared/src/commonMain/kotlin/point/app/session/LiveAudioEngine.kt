package point.app.session

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class AudioPermission { UNKNOWN, GRANTED, DENIED }

/** A little-endian 16-bit mono PCM buffer at [sampleRate] Hz. */
class PcmChunk(val bytes: ByteArray, val sampleRate: Int)

/**
 * Full-duplex PCM audio for the Gemini Live loop. Capture is 16 kHz mono
 * (Gemini's input format); playback follows each chunk's own [PcmChunk.sampleRate]
 * (Gemini's output is 24 kHz). Android uses AudioRecord/AudioTrack; iOS
 * (AVAudioEngine) is Phase 9.
 */
interface LiveAudioEngine {
    val permission: StateFlow<AudioPermission>
    val isCapturing: StateFlow<Boolean>

    fun requestPermission()

    /** Start the mic; collect the returned flow for ~20 ms PCM chunks at 16 kHz. */
    fun startCapture(): Flow<PcmChunk>
    fun stopCapture()

    /** Enqueue a chunk for playback (streamed). */
    suspend fun play(chunk: PcmChunk)

    /** Drop anything queued/playing — call on an AI "interrupted" event. */
    fun clearPlayback()

    fun release()

    companion object {
        const val CAPTURE_SAMPLE_RATE = 16_000
        const val PLAYBACK_SAMPLE_RATE = 24_000
    }
}

@Composable
expect fun rememberLiveAudioEngine(): LiveAudioEngine
