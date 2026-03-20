package dev.kotlinds.fluidsynthkmp

/**
 * JNI bridge to the native fluidsynth_jni shared library.
 * All pointer values are represented as Long (native pointer size).
 */
internal object FluidSynthJni {
    init {
        System.loadLibrary("fluidsynth_jni")
    }

    external fun newSettings(): Long
    external fun deleteSettings(settingsPtr: Long)

    external fun newSynth(settingsPtr: Long): Long
    external fun deleteSynth(synthPtr: Long)

    external fun newAudioDriver(settingsPtr: Long, synthPtr: Long): Long
    external fun deleteDriver(driverPtr: Long)

    external fun sfLoad(synthPtr: Long, path: String, resetPresets: Boolean): Int

    external fun noteOn(synthPtr: Long, channel: Int, key: Int, velocity: Int)
    external fun noteOff(synthPtr: Long, channel: Int, key: Int)
    external fun programChange(synthPtr: Long, channel: Int, program: Int)
    external fun setGain(synthPtr: Long, gain: Float)

    external fun playerNew(synthPtr: Long): Long
    external fun playerAdd(playerPtr: Long, midiPath: String): Int
    external fun playerPlay(playerPtr: Long): Int
    external fun playerStop(playerPtr: Long): Int
    external fun playerDelete(playerPtr: Long)
    external fun playerGetStatus(playerPtr: Long): Int
    external fun playerJoin(playerPtr: Long): Int

    external fun setReverbRoomSize(synthPtr: Long, roomSize: Double): Int
    external fun setReverbDamp(synthPtr: Long, damping: Double): Int
    external fun setReverbWidth(synthPtr: Long, width: Double): Int
    external fun setReverbLevel(synthPtr: Long, level: Double): Int

    external fun setChorusNr(synthPtr: Long, nr: Int): Int
    external fun setChorusLevel(synthPtr: Long, level: Double): Int
    external fun setChorusSpeed(synthPtr: Long, speed: Double): Int
    external fun setChorusDepth(synthPtr: Long, depth: Double): Int

    external fun renderFloat(synthPtr: Long, frames: Int): FloatArray

    external fun playerGetCurrentTick(playerPtr: Long): Int
    external fun playerGetTotalTicks(playerPtr: Long): Int
    external fun playerSeek(playerPtr: Long, ticks: Int): Int
}
