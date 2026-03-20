package dev.kotlinds.fluidsynthkmp

/**
 * Android implementation of [FluidSynthPlayer].
 *
 * Delegates to [FluidSynthJni], which loads the bundled `fluidsynth_jni` native library via JNI.
 * No additional system installation is required on Android.
 *
 * @see FluidSynthPlayer
 */
actual class FluidSynthPlayer actual constructor(sampleRate: Int) {

    private val settingsPtr: Long
    private val synthPtr: Long
    private val driverPtr: Long

    init {
        settingsPtr = FluidSynthJni.newSettings()
        synthPtr = FluidSynthJni.newSynth(settingsPtr)
        driverPtr = FluidSynthJni.newAudioDriver(settingsPtr, synthPtr)
    }

    actual fun loadSoundFont(path: String): Int =
        FluidSynthJni.sfLoad(synthPtr, path, true)

    actual fun noteOn(channel: Int, key: Int, velocity: Int) =
        FluidSynthJni.noteOn(synthPtr, channel, key, velocity)

    actual fun noteOff(channel: Int, key: Int) =
        FluidSynthJni.noteOff(synthPtr, channel, key)

    actual fun programChange(channel: Int, program: Int) =
        FluidSynthJni.programChange(synthPtr, channel, program)

    actual fun setGain(gain: Float) =
        FluidSynthJni.setGain(synthPtr, gain)

    actual fun setReverb(roomSize: Double, damping: Double, width: Double, level: Double) {
        FluidSynthJni.setReverbRoomSize(synthPtr, roomSize)
        FluidSynthJni.setReverbDamp(synthPtr, damping)
        FluidSynthJni.setReverbWidth(synthPtr, width)
        FluidSynthJni.setReverbLevel(synthPtr, level)
    }

    actual fun setChorus(voiceCount: Int, level: Double, speed: Double, depth: Double) {
        FluidSynthJni.setChorusNr(synthPtr, voiceCount)
        FluidSynthJni.setChorusLevel(synthPtr, level)
        FluidSynthJni.setChorusSpeed(synthPtr, speed)
        FluidSynthJni.setChorusDepth(synthPtr, depth)
    }

    actual fun renderFloat(frames: Int): FloatArray =
        FluidSynthJni.renderFloat(synthPtr, frames)

    actual fun close() {
        FluidSynthJni.deleteDriver(driverPtr)
        FluidSynthJni.deleteSynth(synthPtr)
        FluidSynthJni.deleteSettings(settingsPtr)
    }
}
