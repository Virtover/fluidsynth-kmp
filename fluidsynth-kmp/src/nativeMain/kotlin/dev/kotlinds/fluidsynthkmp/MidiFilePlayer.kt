package dev.kotlinds.fluidsynthkmp

import fluidsynth.native.delete_fluid_audio_driver
import fluidsynth.native.delete_fluid_player
import fluidsynth.native.delete_fluid_settings
import fluidsynth.native.delete_fluid_synth
import fluidsynth.native.fluid_player_add
import fluidsynth.native.fluid_player_get_current_tick
import fluidsynth.native.fluid_player_get_status
import fluidsynth.native.fluid_player_get_total_ticks
import fluidsynth.native.fluid_player_join
import fluidsynth.native.fluid_player_play
import fluidsynth.native.fluid_player_seek
import fluidsynth.native.fluid_player_stop
import fluidsynth.native.fluid_synth_sfload
import fluidsynth.native.new_fluid_audio_driver
import fluidsynth.native.new_fluid_player
import fluidsynth.native.new_fluid_settings
import fluidsynth.native.new_fluid_synth
import kotlin.native.concurrent.Worker
import kotlinx.cinterop.ExperimentalForeignApi

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
    sampleRate: Int
) {
    private val settings = new_fluid_settings() ?: error("new_fluid_settings() failed")
    private val synth = new_fluid_synth(settings) ?: error("new_fluid_synth() failed")
    private val driver = new_fluid_audio_driver(settings, synth)
    private val player = run {
        fluid_synth_sfload(synth, soundFontPath, 1)
        new_fluid_player(synth) ?: error("new_fluid_player() failed")
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

    actual val durationTicks: Long
        get() = fluid_player_get_total_ticks(player).toLong()

    actual fun close() {
        fluid_player_stop(player)
        delete_fluid_player(player)
        driver?.let { delete_fluid_audio_driver(it) }
        delete_fluid_synth(synth)
        delete_fluid_settings(settings)
    }
}
