package dev.kotlinds.fluidsynthkmp

/** FLUID_PLAYER_PLAYING status value from FluidSynth headers */
private const val FLUID_PLAYER_PLAYING = 1

/**
 * Android implementation of [MidiFilePlayer].
 *
 * Delegates to [FluidSynthJni], which loads the bundled `fluidsynth_jni` native library via JNI.
 * The completion callback is dispatched on a daemon thread via [Thread].
 *
 * @see MidiFilePlayer
 */
actual class MidiFilePlayer actual constructor(
    soundFontPath: String,
    midiPath: String,
    config: AudioConfig,
) {
    private val context = FluidSynthContext(config)
    private val playerPtr: Long
    private var pausedAtTick: Int = -1

    init {
        FluidSynthJni.sfLoad(context.synthPtr, soundFontPath, true)
        playerPtr = FluidSynthJni.playerNew(context.synthPtr)
        FluidSynthJni.playerAdd(playerPtr, midiPath)
    }

    actual fun play(onComplete: (() -> Unit)?) {
        if (pausedAtTick >= 0) {
            FluidSynthJni.playerSeek(playerPtr, pausedAtTick)
            pausedAtTick = -1
        }
        FluidSynthJni.playerPlay(playerPtr)
        if (onComplete != null) {
            Thread {
                FluidSynthJni.playerJoin(playerPtr)
                onComplete()
            }.also { it.isDaemon = true }.start()
        }
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
        context.close()
    }
}
