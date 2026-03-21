package dev.kotlinds.fluidsynthkmp

import com.sun.jna.Pointer

/** FLUID_PLAYER_PLAYING status value from FluidSynth headers */
private const val FLUID_PLAYER_PLAYING = 1

/**
 * JVM implementation of [MidiFilePlayer].
 *
 * Delegates to [FluidSynthLib] (JNA binding) to call the system FluidSynth shared library.
 * The completion callback is dispatched on a daemon thread via [Thread].
 *
 * @see MidiFilePlayer
 */
actual class MidiFilePlayer actual constructor(
    soundFontPath: String,
    midiPath: String,
    config: AudioConfig,
) {
    private val lib = FluidSynthLib.INSTANCE
    private val context = FluidSynthContext(config)
    private val player: Pointer
    private var pausedAtTick: Int = -1

    init {
        lib.fluid_synth_sfload(context.synth, soundFontPath, 1)
        player = lib.new_fluid_player(context.synth) ?: error("new_fluid_player() failed")
        lib.fluid_player_add(player, midiPath)
    }

    actual fun play(onComplete: (() -> Unit)?) {
        if (pausedAtTick >= 0) {
            lib.fluid_player_seek(player, pausedAtTick)
            pausedAtTick = -1
        }
        lib.fluid_player_play(player)
        if (onComplete != null) {
            Thread {
                lib.fluid_player_join(player)
                onComplete()
            }.also { it.isDaemon = true }.start()
        }
    }

    actual fun stop() {
        pausedAtTick = -1
        lib.fluid_player_stop(player)
    }

    actual fun pause() {
        pausedAtTick = lib.fluid_player_get_current_tick(player)
        lib.fluid_player_stop(player)
    }

    actual fun seekTo(tick: Long) {
        lib.fluid_player_seek(player, tick.toInt())
    }

    actual val isPlaying: Boolean
        get() = pausedAtTick < 0 && lib.fluid_player_get_status(player) == FLUID_PLAYER_PLAYING

    actual val durationTicks: Long
        get() = lib.fluid_player_get_total_ticks(player).toLong()

    actual fun close() {
        lib.fluid_player_stop(player)
        lib.delete_fluid_player(player)
        context.close()
    }
}
