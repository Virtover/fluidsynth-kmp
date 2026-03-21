package dev.kotlinds.fluidsynthkmp

import fluidsynth.native.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.native.concurrent.Worker

/** fluid_player_status_t::FLUID_PLAYER_PLAYING == 1 */
private const val FLUID_PLAYER_PLAYING_VALUE = 1

/**
 * Native (cinterop) implementation of [MidiFilePlayer].
 *
 * Delegates directly to the FluidSynth C API via Kotlin/Native cinterop.
 *
 * @see MidiFilePlayer
 */
@OptIn(ExperimentalForeignApi::class)
actual class MidiFilePlayer actual constructor(
    soundFontPath: String,
    midiPath: String,
    config: AudioConfig,
) {
    private val context = FluidSynthContext(config)
    private val player = run {
        fluid_synth_sfload(context.synth, soundFontPath, 1)
        new_fluid_player(context.synth) ?: error("new_fluid_player() failed")
    }
    private var pausedAtTick: Int = -1

    init {
        fluid_player_add(player, midiPath)
    }

    actual fun play(onComplete: (() -> Unit)?) {
        if (pausedAtTick >= 0) {
            fluid_player_seek(player, pausedAtTick)
            pausedAtTick = -1
        }
        fluid_player_play(player)
        if (onComplete != null) {
            val worker = Worker.start()
            worker.executeAfter(0L) {
                fluid_player_join(player)
                onComplete()
            }
        }
    }

    actual fun stop() {
        pausedAtTick = -1
        fluid_player_stop(player)
    }

    actual fun pause() {
        pausedAtTick = fluid_player_get_current_tick(player)
        fluid_player_stop(player)
    }

    actual fun seekTo(tick: Long) {
        fluid_player_seek(player, tick.toInt())
    }

    actual val isPlaying: Boolean
        get() = pausedAtTick < 0 && fluid_player_get_status(player) == FLUID_PLAYER_PLAYING_VALUE

    actual val currentTick: Long
        get() = if (pausedAtTick >= 0) pausedAtTick.toLong() else fluid_player_get_current_tick(player).toLong()

    actual val durationTicks: Long
        get() = fluid_player_get_total_ticks(player).toLong()

    actual fun close() {
        fluid_player_stop(player)
        delete_fluid_player(player)
        context.close()
    }
}
