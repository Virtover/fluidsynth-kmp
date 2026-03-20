package dev.kotlinds.fluidsynthkmp

import fluidsynth.native.delete_fluid_audio_driver
import fluidsynth.native.delete_fluid_settings
import fluidsynth.native.delete_fluid_synth
import fluidsynth.native.fluid_synth_noteoff
import fluidsynth.native.fluid_synth_noteon
import fluidsynth.native.fluid_synth_program_change
import fluidsynth.native.fluid_synth_set_chorus_group_depth
import fluidsynth.native.fluid_synth_set_chorus_group_level
import fluidsynth.native.fluid_synth_set_chorus_group_nr
import fluidsynth.native.fluid_synth_set_chorus_group_speed
import fluidsynth.native.fluid_synth_set_gain
import fluidsynth.native.fluid_synth_set_reverb_group_damp
import fluidsynth.native.fluid_synth_set_reverb_group_level
import fluidsynth.native.fluid_synth_set_reverb_group_roomsize
import fluidsynth.native.fluid_synth_set_reverb_group_width
import fluidsynth.native.fluid_synth_sfload
import fluidsynth.native.fluid_synth_write_float
import fluidsynth.native.new_fluid_audio_driver
import fluidsynth.native.new_fluid_settings
import fluidsynth.native.new_fluid_synth
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.FloatVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped

/**
 * Native (cinterop) implementation of [FluidSynthPlayer].
 *
 * Delegates directly to the FluidSynth C API via Kotlin/Native cinterop.
 *
 * @see FluidSynthPlayer
 */
@OptIn(ExperimentalForeignApi::class)
actual class FluidSynthPlayer actual constructor(sampleRate: Int) {

    private val settings = new_fluid_settings() ?: error("new_fluid_settings() failed")
    private val synth = new_fluid_synth(settings) ?: error("new_fluid_synth() failed")
    private val driver = new_fluid_audio_driver(settings, synth)

    actual fun loadSoundFont(path: String): Int =
        fluid_synth_sfload(synth, path, 1)

    actual fun noteOn(channel: Int, key: Int, velocity: Int) {
        fluid_synth_noteon(synth, channel, key, velocity)
    }

    actual fun noteOff(channel: Int, key: Int) {
        fluid_synth_noteoff(synth, channel, key)
    }

    actual fun programChange(channel: Int, program: Int) {
        fluid_synth_program_change(synth, channel, program)
    }

    actual fun setGain(gain: Float) {
        fluid_synth_set_gain(synth, gain)
    }

    actual fun setReverb(roomSize: Double, damping: Double, width: Double, level: Double) {
        fluid_synth_set_reverb_group_roomsize(synth, -1, roomSize)
        fluid_synth_set_reverb_group_damp(synth, -1, damping)
        fluid_synth_set_reverb_group_width(synth, -1, width)
        fluid_synth_set_reverb_group_level(synth, -1, level)
    }

    actual fun setChorus(voiceCount: Int, level: Double, speed: Double, depth: Double) {
        fluid_synth_set_chorus_group_nr(synth, -1, voiceCount)
        fluid_synth_set_chorus_group_level(synth, -1, level)
        fluid_synth_set_chorus_group_speed(synth, -1, speed)
        fluid_synth_set_chorus_group_depth(synth, -1, depth)
    }

    actual fun renderFloat(frames: Int): FloatArray = memScoped {
        val left = allocArray<FloatVar>(frames)
        val right = allocArray<FloatVar>(frames)
        fluid_synth_write_float(synth, frames, left, 0, 1, right, 0, 1)
        FloatArray(frames * 2) { i ->
            if (i % 2 == 0) left[i / 2] else right[i / 2]
        }
    }

    actual fun close() {
        driver?.let { delete_fluid_audio_driver(it) }
        delete_fluid_synth(synth)
        delete_fluid_settings(settings)
    }
}
