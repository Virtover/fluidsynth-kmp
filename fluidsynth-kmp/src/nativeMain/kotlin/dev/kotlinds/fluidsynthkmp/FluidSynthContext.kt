package dev.kotlinds.fluidsynthkmp

import fluidsynth.native.*
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Holds the native FluidSynth resources shared by [FluidSynthPlayer] and [MidiFilePlayer].
 *
 * Initialization order: settings → configure settings → synth → configure synth → audio driver.
 */
@OptIn(ExperimentalForeignApi::class)
internal class FluidSynthContext(config: AudioConfig) {
    val settings = run {
        val s = new_fluid_settings() ?: error("new_fluid_settings() failed")
        fluid_settings_setnum(s, "synth.sample-rate", config.sampleRate.toDouble())
        fluid_settings_setint(s, "audio.period-size", config.periodSize)
        fluid_settings_setint(s, "audio.periods", config.periods)
        s
    }

    val synth = run {
        val s = new_fluid_synth(settings) ?: error("new_fluid_synth() failed")
        fluid_synth_set_interp_method(s, -1, config.interpolation)
        s
    }

    val driver = new_fluid_audio_driver(settings, synth)

    fun setInterpolation(interpolation: Int) {
        fluid_synth_set_interp_method(synth, -1, interpolation)
    }

    fun close() {
        driver?.let { delete_fluid_audio_driver(it) }
        delete_fluid_synth(synth)
        delete_fluid_settings(settings)
    }
}
