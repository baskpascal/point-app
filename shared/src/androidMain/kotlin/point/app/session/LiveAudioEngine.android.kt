package point.app.session

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.concurrent.thread

/**
 * VoIP-style full-duplex audio: capture on the `VOICE_COMMUNICATION` source and
 * play back on the matching output while `MODE_IN_COMMUNICATION` is set, so the
 * platform's hardware acoustic echo canceller actually engages and removes the
 * speaker from the mic in real time. No half-duplex muting — the user can talk
 * over the assistant.
 */
internal class AndroidLiveAudioEngine(private val appContext: Context) : LiveAudioEngine {

    private val am = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _permission = MutableStateFlow(AudioPermission.UNKNOWN)
    override val permission: StateFlow<AudioPermission> = _permission.asStateFlow()

    private val _capturing = MutableStateFlow(false)
    override val isCapturing: StateFlow<Boolean> = _capturing.asStateFlow()

    private var requestLauncher: (() -> Unit)? = null
    @Volatile private var recording = false
    private var recordThread: Thread? = null
    private val fx = mutableListOf<android.media.audiofx.AudioEffect>()

    private var track: AudioTrack? = null
    private var trackRate = 0
    private var bufferedBytes = 0
    private var primeThreshold = 0
    private var savedMode = AudioManager.MODE_NORMAL

    fun attach(launcher: () -> Unit) {
        requestLauncher = launcher
        refreshPermission()
    }

    fun detach() {
        stopCapture()
        release()
        requestLauncher = null
    }

    fun refreshPermission() {
        val granted = ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) _permission.value = AudioPermission.GRANTED
        else if (_permission.value != AudioPermission.DENIED) _permission.value = AudioPermission.UNKNOWN
    }

    fun onPermissionResult(granted: Boolean) {
        _permission.value = if (granted) AudioPermission.GRANTED else AudioPermission.DENIED
    }

    override fun requestPermission() {
        if (_permission.value == AudioPermission.GRANTED) return
        requestLauncher?.invoke()
    }

    private fun enterCommMode() {
        savedMode = am.mode
        am.mode = AudioManager.MODE_IN_COMMUNICATION
        // Keep output on the loudspeaker (comm mode defaults to the earpiece).
        if (Build.VERSION.SDK_INT >= 31) {
            am.availableCommunicationDevices
                .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                ?.let { runCatching { am.setCommunicationDevice(it) } }
        } else {
            @Suppress("DEPRECATION") run { am.isSpeakerphoneOn = true }
        }
    }

    private fun leaveCommMode() {
        runCatching {
            if (Build.VERSION.SDK_INT >= 31) am.clearCommunicationDevice()
            else @Suppress("DEPRECATION") run { am.isSpeakerphoneOn = false }
        }
        am.mode = savedMode
    }

    override fun startCapture(): Flow<PcmChunk> = callbackFlow {
        val rate = LiveAudioEngine.CAPTURE_SAMPLE_RATE
        val minBuf = AudioRecord.getMinBufferSize(
            rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
        ).coerceAtLeast(rate)
        val chunkBytes = rate / 50 * 2 // ~20 ms of 16-bit mono

        enterCommMode()

        val record = runCatching {
            AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION, // enables platform AEC/NS/AGC pre-processing
                rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                minBuf,
            )
        }.getOrElse { leaveCommMode(); close(it); return@callbackFlow }
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release(); leaveCommMode()
            close(IllegalStateException("AudioRecord failed to initialize"))
            return@callbackFlow
        }
        val session = record.audioSessionId
        runCatching { if (AcousticEchoCanceler.isAvailable()) AcousticEchoCanceler.create(session)?.also { it.enabled = true; fx += it } }
        runCatching { if (NoiseSuppressor.isAvailable()) NoiseSuppressor.create(session)?.also { it.enabled = true; fx += it } }
        // No AGC: it pumps up the room-tone / AEC residual between words, which keeps
        // the server VAD from ever hearing "silence" and delays the model's reply.

        record.startRecording()
        recording = true
        _capturing.value = true
        println("[audio] capture started @ ${rate}Hz (comm mode, AEC=${AcousticEchoCanceler.isAvailable()})")

        recordThread = thread(name = "point-mic") {
            val buf = ByteArray(chunkBytes)
            var err: Throwable? = null
            while (recording) {
                val n = record.read(buf, 0, buf.size)
                when {
                    n > 0 -> trySend(PcmChunk(buf.copyOf(n), rate))
                    n < 0 -> { err = IllegalStateException("AudioRecord.read failed ($n)"); break }
                }
            }
            runCatching { record.stop() }
            record.release()
            fx.forEach { runCatching { it.release() } }
            fx.clear()
            println("[audio] capture stopped${err?.let { " ($it)" } ?: ""}")
            // Let the collector know so the session can re-arm the mic.
            if (err != null) close(err)
        }

        awaitClose {
            recording = false
            _capturing.value = false
            recordThread?.join(500)
            recordThread = null
            leaveCommMode()
        }
    }

    override fun stopCapture() {
        recording = false
        _capturing.value = false
    }

    override suspend fun play(chunk: PcmChunk) {
        val t = ensureTrack(chunk.sampleRate)
        t.write(chunk.bytes, 0, chunk.bytes.size)
        bufferedBytes += chunk.bytes.size
        // Prime ~150 ms before starting so bursty delivery doesn't underrun (chop).
        if (t.playState != AudioTrack.PLAYSTATE_PLAYING && bufferedBytes >= primeThreshold) t.play()
    }

    private fun ensureTrack(rate: Int): AudioTrack {
        track?.let { if (trackRate == rate) return it }
        track?.runCatching { stop(); release() }
        bufferedBytes = 0
        primeThreshold = rate / 1000 * 150 * 2
        val minBuf = AudioTrack.getMinBufferSize(
            rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
        ).coerceAtLeast(rate * 2) // ~1 s
        val t = AudioTrack(
            AudioAttributes.Builder()
                // Match the capture path so the echo canceller has a reference signal.
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(rate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build(),
            minBuf,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        )
        track = t
        trackRate = rate
        println("[audio] playback track @ ${rate}Hz")
        return t
    }

    override fun clearPlayback() {
        track?.runCatching { pause(); flush() }
        bufferedBytes = 0
    }

    override fun release() {
        track?.runCatching { stop(); release() }
        track = null
        trackRate = 0
        if (am.mode == AudioManager.MODE_IN_COMMUNICATION) leaveCommMode()
    }
}

@Composable
actual fun rememberLiveAudioEngine(): LiveAudioEngine {
    val context = LocalContext.current
    val engine = remember { AndroidLiveAudioEngine(context.applicationContext) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> engine.onPermissionResult(granted) }
    DisposableEffect(Unit) {
        engine.attach { launcher.launch(Manifest.permission.RECORD_AUDIO) }
        onDispose { engine.detach() }
    }
    return engine
}
