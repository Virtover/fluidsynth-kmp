package dev.kotlinds.fluidsynthkmp

/**
 * Android implementation of [FluidSynthPlayer].
 *
 * Delegates to [FluidSynthJni], which loads the bundled `fluidsynth_jni` native library via JNI.
 * No additional system installation is required on Android.
 *
 * @see FluidSynthPlayer
 */
actual class FluidSynthPlayer actual constructor(config: AudioConfig) {
    private val context = FluidSynthContext(config)

    actual fun loadSoundFont(path: String): Int =
        FluidSynthJni.sfLoad(context.synthPtr, path, true)

    actual fun noteOn(channel: Int, key: Int, velocity: Int) =
        FluidSynthJni.noteOn(context.synthPtr, channel, key, velocity)

    actual fun noteOff(channel: Int, key: Int) =
        FluidSynthJni.noteOff(context.synthPtr, channel, key)

    actual fun programChange(channel: Int, program: Int) =
        FluidSynthJni.programChange(context.synthPtr, channel, program)

    actual fun setGain(gain: Float) =
        FluidSynthJni.setGain(context.synthPtr, gain)

    actual fun setInterpolation(interpolation: Int) {
        context.setInterpolation(interpolation)
    }

    actual fun setReverb(roomSize: Double, damping: Double, width: Double, level: Double) {
        FluidSynthJni.setReverbRoomSize(context.synthPtr, roomSize)
        FluidSynthJni.setReverbDamp(context.synthPtr, damping)
        FluidSynthJni.setReverbWidth(context.synthPtr, width)
        FluidSynthJni.setReverbLevel(context.synthPtr, level)
    }

    actual fun setChorus(voiceCount: Int, level: Double, speed: Double, depth: Double) {
        FluidSynthJni.setChorusNr(context.synthPtr, voiceCount)
        FluidSynthJni.setChorusLevel(context.synthPtr, level)
        FluidSynthJni.setChorusSpeed(context.synthPtr, speed)
        FluidSynthJni.setChorusDepth(context.synthPtr, depth)
    }

    actual fun renderFloat(frames: Int): FloatArray =
        FluidSynthJni.renderFloat(context.synthPtr, frames)

    actual fun close() {
        context.close()
    }
}
