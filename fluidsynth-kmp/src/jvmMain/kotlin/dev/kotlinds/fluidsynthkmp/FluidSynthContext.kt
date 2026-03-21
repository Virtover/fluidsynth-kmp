package dev.kotlinds.fluidsynthkmp

import com.sun.jna.Pointer

/**
 * Holds the native FluidSynth resources shared by [FluidSynthPlayer] and [MidiFilePlayer].
 *
 * Initialization order: settings → configure settings → synth → configure synth → audio driver.
 */
internal class FluidSynthContext(config: AudioConfig) {
    private val lib = FluidSynthLib.INSTANCE

    val settings: Pointer = lib.new_fluid_settings() ?: error("new_fluid_settings() failed")
    val synth: Pointer
    val driver: Pointer?

    init {
        lib.fluid_settings_setnum(settings, "synth.sample-rate", config.sampleRate.toDouble())
        lib.fluid_settings_setint(settings, "audio.period-size", config.periodSize)
        lib.fluid_settings_setint(settings, "audio.periods", config.periods)

        synth = lib.new_fluid_synth(settings) ?: error("new_fluid_synth() failed")
        lib.fluid_synth_set_interp_method(synth, -1, config.interpolation)

        driver = lib.new_fluid_audio_driver(settings, synth)
    }

    fun setInterpolation(interpolation: Int) {
        lib.fluid_synth_set_interp_method(synth, -1, interpolation)
    }

    fun close() {
        driver?.let { lib.delete_fluid_audio_driver(it) }
        lib.delete_fluid_synth(synth)
        lib.delete_fluid_settings(settings)
    }
}
