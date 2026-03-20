package dev.kotlinds.fluidsynthkmp

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

/**
 * JNA binding for the FluidSynth native library.
 * Loads the system-installed `fluidsynth` shared library at runtime.
 */
internal interface FluidSynthLib : Library {

    // Settings
    fun new_fluid_settings(): Pointer?
    fun delete_fluid_settings(settings: Pointer)

    // Synth
    fun new_fluid_synth(settings: Pointer): Pointer?
    fun delete_fluid_synth(synth: Pointer)

    // Audio driver
    fun new_fluid_audio_driver(settings: Pointer, synth: Pointer): Pointer?
    fun delete_fluid_audio_driver(driver: Pointer)

    // SoundFont loading
    fun fluid_synth_sfload(synth: Pointer, filename: String, reset_presets: Int): Int

    // Note control
    fun fluid_synth_noteon(synth: Pointer, chan: Int, key: Int, vel: Int): Int
    fun fluid_synth_noteoff(synth: Pointer, chan: Int, key: Int): Int
    fun fluid_synth_program_change(synth: Pointer, chan: Int, program: Int): Int
    fun fluid_synth_set_gain(synth: Pointer, gain: Float)

    // MIDI player
    fun new_fluid_player(synth: Pointer): Pointer?
    fun delete_fluid_player(player: Pointer)
    fun fluid_player_add(player: Pointer, midifile: String): Int
    fun fluid_player_play(player: Pointer): Int
    fun fluid_player_stop(player: Pointer): Int
    fun fluid_player_get_status(player: Pointer): Int
    fun fluid_player_get_current_tick(player: Pointer): Int
    fun fluid_player_get_total_ticks(player: Pointer): Int
    fun fluid_player_seek(player: Pointer, ticks: Int): Int
    fun fluid_player_join(player: Pointer): Int

    // Reverb
    fun fluid_synth_set_reverb_group_roomsize(synth: Pointer, fx_group: Int, roomsize: Double): Int
    fun fluid_synth_set_reverb_group_damp(synth: Pointer, fx_group: Int, damping: Double): Int
    fun fluid_synth_set_reverb_group_width(synth: Pointer, fx_group: Int, width: Double): Int
    fun fluid_synth_set_reverb_group_level(synth: Pointer, fx_group: Int, level: Double): Int

    // Chorus
    fun fluid_synth_set_chorus_group_nr(synth: Pointer, fx_group: Int, nr: Int): Int
    fun fluid_synth_set_chorus_group_level(synth: Pointer, fx_group: Int, level: Double): Int
    fun fluid_synth_set_chorus_group_speed(synth: Pointer, fx_group: Int, speed: Double): Int
    fun fluid_synth_set_chorus_group_depth(synth: Pointer, fx_group: Int, depth_ms: Double): Int

    // Render
    fun fluid_synth_write_float(synth: Pointer, len: Int, lout: FloatArray, loff: Int, lincr: Int, rout: FloatArray, roff: Int, rincr: Int): Int

    companion object {
        val INSTANCE: FluidSynthLib by lazy {
            Native.load("fluidsynth", FluidSynthLib::class.java)
        }
    }
}
