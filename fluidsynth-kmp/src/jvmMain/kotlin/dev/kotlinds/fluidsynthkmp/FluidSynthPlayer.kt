package dev.kotlinds.fluidsynthkmp

import com.sun.jna.Pointer

actual class FluidSynthPlayer actual constructor(sampleRate: Int) {

    private val lib = FluidSynthLib.INSTANCE
    private val settings: Pointer
    private val synth: Pointer
    private val driver: Pointer?

    init {
        settings = lib.new_fluid_settings() ?: error("new_fluid_settings() failed")
        synth = lib.new_fluid_synth(settings) ?: error("new_fluid_synth() failed")
        driver = lib.new_fluid_audio_driver(settings, synth)
    }

    actual fun loadSoundFont(path: String): Int =
        lib.fluid_synth_sfload(synth, path, 1)

    actual fun noteOn(channel: Int, key: Int, velocity: Int) {
        lib.fluid_synth_noteon(synth, channel, key, velocity)
    }

    actual fun noteOff(channel: Int, key: Int) {
        lib.fluid_synth_noteoff(synth, channel, key)
    }

    actual fun programChange(channel: Int, program: Int) {
        lib.fluid_synth_program_change(synth, channel, program)
    }

    actual fun setGain(gain: Float) {
        lib.fluid_synth_set_gain(synth, gain)
    }

    actual fun setReverb(roomSize: Double, damping: Double, width: Double, level: Double) {
        lib.fluid_synth_set_reverb_group_roomsize(synth, -1, roomSize)
        lib.fluid_synth_set_reverb_group_damp(synth, -1, damping)
        lib.fluid_synth_set_reverb_group_width(synth, -1, width)
        lib.fluid_synth_set_reverb_group_level(synth, -1, level)
    }

    actual fun setChorus(voiceCount: Int, level: Double, speed: Double, depth: Double) {
        lib.fluid_synth_set_chorus_group_nr(synth, -1, voiceCount)
        lib.fluid_synth_set_chorus_group_level(synth, -1, level)
        lib.fluid_synth_set_chorus_group_speed(synth, -1, speed)
        lib.fluid_synth_set_chorus_group_depth(synth, -1, depth)
    }

    actual fun renderFloat(frames: Int): FloatArray {
        val left = FloatArray(frames)
        val right = FloatArray(frames)
        lib.fluid_synth_write_float(synth, frames, left, 0, 1, right, 0, 1)
        val result = FloatArray(frames * 2)
        for (i in 0 until frames) {
            result[i * 2] = left[i]
            result[i * 2 + 1] = right[i]
        }
        return result
    }

    actual fun close() {
        driver?.let { lib.delete_fluid_audio_driver(it) }
        lib.delete_fluid_synth(synth)
        lib.delete_fluid_settings(settings)
    }
}
