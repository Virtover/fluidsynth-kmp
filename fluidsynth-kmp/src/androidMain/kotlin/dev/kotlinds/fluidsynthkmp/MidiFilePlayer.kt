package dev.kotlinds.fluidsynthkmp

/** FLUID_PLAYER_PLAYING status value from FluidSynth headers */
private const val FLUID_PLAYER_PLAYING = 1

actual class MidiFilePlayer actual constructor(
    soundFontPath: String,
    midiPath: String,
    sampleRate: Int
) {
    private val settingsPtr: Long
    private val synthPtr: Long
    private val driverPtr: Long
    private val playerPtr: Long
    private var pausedAtTick: Int = -1

    init {
        settingsPtr = FluidSynthJni.newSettings()
        synthPtr = FluidSynthJni.newSynth(settingsPtr)
        driverPtr = FluidSynthJni.newAudioDriver(settingsPtr, synthPtr)
        FluidSynthJni.sfLoad(synthPtr, soundFontPath, true)
        playerPtr = FluidSynthJni.playerNew(synthPtr)
        FluidSynthJni.playerAdd(playerPtr, midiPath)
    }

    actual fun play() {
        if (pausedAtTick >= 0) {
            FluidSynthJni.playerSeek(playerPtr, pausedAtTick)
            pausedAtTick = -1
        }
        FluidSynthJni.playerPlay(playerPtr)
    }

    actual fun stop() {
        pausedAtTick = -1
        FluidSynthJni.playerStop(playerPtr)
    }

    actual fun pause() {
        pausedAtTick = FluidSynthJni.playerGetCurrentTick(playerPtr)
        FluidSynthJni.playerStop(playerPtr)
    }

    actual fun seekTo(tick: Long) {
        FluidSynthJni.playerSeek(playerPtr, tick.toInt())
    }

    actual val isPlaying: Boolean
        get() = pausedAtTick < 0 && FluidSynthJni.playerGetStatus(playerPtr) == FLUID_PLAYER_PLAYING

    actual val durationTicks: Long
        get() = FluidSynthJni.playerGetTotalTicks(playerPtr).toLong()

    actual fun close() {
        FluidSynthJni.playerStop(playerPtr)
        FluidSynthJni.playerDelete(playerPtr)
        FluidSynthJni.deleteDriver(driverPtr)
        FluidSynthJni.deleteSynth(synthPtr)
        FluidSynthJni.deleteSettings(settingsPtr)
    }
}
