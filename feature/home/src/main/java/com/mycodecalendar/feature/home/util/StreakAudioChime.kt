package com.mycodecalendar.feature.home.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * StreakAudioChime — Synthesizes a crisp, game-like 1-second celebratory chime
 * for streak completion and new daily check-ins using native Android AudioTrack.
 */
object StreakAudioChime {

    suspend fun playCelebrationChime() = withContext(Dispatchers.Default) {
        try {
            val sampleRate = 44100
            val durationSec = 1.05
            val numSamples = (sampleRate * durationSec).toInt()
            val audioBuffer = ShortArray(numSamples)

            // Musical Notes: C5 (523Hz), E5 (659Hz), G5 (784Hz), C6 (1046Hz)
            val notes = listOf(
                NoteEvent(freq = 523.25, startSec = 0.00, duration = 0.65, amplitude = 0.65),
                NoteEvent(freq = 659.25, startSec = 0.12, duration = 0.65, amplitude = 0.70),
                NoteEvent(freq = 783.99, startSec = 0.24, duration = 0.70, amplitude = 0.75),
                NoteEvent(freq = 1046.50, startSec = 0.36, duration = 0.68, amplitude = 0.95),
                NoteEvent(freq = 1318.51, startSec = 0.40, duration = 0.64, amplitude = 0.50) // E6 sparkle
            )

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                var sampleValue = 0.0

                for (note in notes) {
                    if (t >= note.startSec && t <= note.startSec + note.duration) {
                        val localT = t - note.startSec
                        // Smooth attack (15ms) + gentle exponential decay
                        val attack = (localT / 0.018).coerceAtMost(1.0)
                        val decay = exp(-localT * 4.2)
                        val env = attack * decay

                        // Warm bell harmonic: Fundamental + Octave harmonic
                        val wave = 0.75 * sin(2.0 * PI * note.freq * localT) +
                                   0.25 * sin(2.0 * PI * note.freq * 2.0 * localT)

                        sampleValue += wave * env * note.amplitude
                    }
                }

                // Master limiter and conversion to 16-bit PCM
                val clamped = (sampleValue * 0.55).coerceIn(-0.98, 0.98)
                audioBuffer[i] = (clamped * Short.MAX_VALUE).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(audioBuffer, 0, numSamples)
            audioTrack.play()

            // Release after completion
            kotlinx.coroutines.delay(1150L)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // Audio playback is non-fatal
        }
    }

    private data class NoteEvent(
        val freq: Double,
        val startSec: Double,
        val duration: Double,
        val amplitude: Double
    )
}
