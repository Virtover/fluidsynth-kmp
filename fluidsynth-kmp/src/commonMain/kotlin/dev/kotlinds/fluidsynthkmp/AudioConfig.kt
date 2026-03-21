package dev.kotlinds.fluidsynthkmp

/**
 * Audio configuration for the FluidSynth engine.
 *
 * These settings are applied during synthesizer initialization and affect
 * audio quality, latency, and resource usage.
 *
 * @param sampleRate Output sample rate in Hz. Defaults to 44100.
 * @param interpolation Resampling interpolation quality. Defaults to [Interpolation.HIGH].
 * @param periodSize Number of audio frames per buffer period. Smaller values reduce latency
 *   but increase CPU overhead. Defaults to 64 (tuned for mobile).
 * @param periods Number of buffer periods. Defaults to 2.
 */
data class AudioConfig(
    val sampleRate: Int = 44100,
    val interpolation: Int = Interpolation.HIGH,
    val periodSize: Int = 64,
    val periods: Int = 2,
)
