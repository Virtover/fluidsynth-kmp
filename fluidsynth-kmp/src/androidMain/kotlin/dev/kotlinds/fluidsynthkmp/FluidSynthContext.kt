package dev.kotlinds.fluidsynthkmp

/**
 * Holds the native FluidSynth resources shared by [FluidSynthPlayer] and [MidiFilePlayer].
 *
 * Initialization order: settings → configure settings → synth → configure synth → audio driver.
 */
internal class FluidSynthContext(config: AudioConfig) {
    val settingsPtr: Long
    val synthPtr: Long
    val driverPtr: Long

    init {
        settingsPtr = FluidSynthJni.newSettings()
        FluidSynthJni.setSettingsNum(settingsPtr, "synth.sample-rate", config.sampleRate.toDouble())
        FluidSynthJni.setSettingsInt(settingsPtr, "audio.period-size", config.periodSize)
        FluidSynthJni.setSettingsInt(settingsPtr, "audio.periods", config.periods)

        synthPtr = FluidSynthJni.newSynth(settingsPtr)
        FluidSynthJni.setInterpMethod(synthPtr, -1, config.interpolation)

        driverPtr = FluidSynthJni.newAudioDriver(settingsPtr, synthPtr)
    }

    fun setInterpolation(interpolation: Int) {
        FluidSynthJni.setInterpMethod(synthPtr, -1, interpolation)
    }

    fun close() {
        FluidSynthJni.deleteDriver(driverPtr)
        FluidSynthJni.deleteSynth(synthPtr)
        FluidSynthJni.deleteSettings(settingsPtr)
    }
}
