package dev.kotlinds.fluidsynthkmp

import fluidsynth.native.*
import kotlinx.cinterop.*

/**
 * Native (cinterop) implementation of [FluidSynthPlayer].
 *
 * Delegates directly to the FluidSynth C API via Kotlin/Native cinterop.
 *
 * @see FluidSynthPlayer
 */
@OptIn(ExperimentalForeignApi::class)
actual class FluidSynthPlayer actual constructor(config: AudioConfig) {
    private val context = FluidSynthContext(config)

    actual fun loadSoundFont(path: String): Int =
        fluid_synth_sfload(context.synth, path, 1)

    actual fun noteOn(channel: Int, key: Int, velocity: Int) {
        fluid_synth_noteon(context.synth, channel, key, velocity)
    }

    actual fun noteOff(channel: Int, key: Int) {
        fluid_synth_noteoff(context.synth, channel, key)
    }

    actual fun programChange(channel: Int, program: Int) {
        fluid_synth_program_change(context.synth, channel, program)
    }

    actual fun setGain(gain: Float) {
        fluid_synth_set_gain(context.synth, gain)
    }

    actual fun setInterpolation(interpolation: Int) {
        context.setInterpolation(interpolation)
    }

    actual fun setReverb(roomSize: Double, damping: Double, width: Double, level: Double) {
        fluid_synth_set_reverb_group_roomsize(context.synth, -1, roomSize)
        fluid_synth_set_reverb_group_damp(context.synth, -1, damping)
        fluid_synth_set_reverb_group_width(context.synth, -1, width)
        fluid_synth_set_reverb_group_level(context.synth, -1, level)
    }

    actual fun setChorus(voiceCount: Int, level: Double, speed: Double, depth: Double) {
        fluid_synth_set_chorus_group_nr(context.synth, -1, voiceCount)
        fluid_synth_set_chorus_group_level(context.synth, -1, level)
        fluid_synth_set_chorus_group_speed(context.synth, -1, speed)
        fluid_synth_set_chorus_group_depth(context.synth, -1, depth)
    }

    actual fun renderFloat(frames: Int): FloatArray = memScoped {
        val left = allocArray<FloatVar>(frames)
        val right = allocArray<FloatVar>(frames)
        fluid_synth_write_float(context.synth, frames, left, 0, 1, right, 0, 1)
        FloatArray(frames * 2) { i ->
            if (i % 2 == 0) left[i / 2] else right[i / 2]
        }
    }

    actual fun close() {
        context.close()
    }
}
