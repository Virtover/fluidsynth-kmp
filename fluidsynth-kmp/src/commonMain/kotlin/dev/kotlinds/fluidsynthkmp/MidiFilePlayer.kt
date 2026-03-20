package dev.kotlinds.fluidsynthkmp

/**
 * A MIDI file player backed by FluidSynth.
 *
 * Loads a SoundFont and a MIDI file, and plays back audio through the platform's
 * default audio driver. Supports play, pause, stop, and seek operations.
 *
 * @param soundFontPath Absolute path to the SoundFont (.sf2) file.
 * @param midiPath Absolute path to the MIDI (.mid) file.
 * @param sampleRate The sample rate in Hz. Defaults to 44100.
 */
expect class MidiFilePlayer(soundFontPath: String, midiPath: String, sampleRate: Int = 44100) {

    /**
     * Starts or resumes playback.
     *
     * If the player was paused, playback resumes from the position where [pause] was called.
     *
     * @param onComplete Optional callback invoked on a background thread when the MIDI file
     *   finishes playing. Not called if playback is stopped manually via [stop].
     */
    fun play(onComplete: (() -> Unit)? = null)

    /**
     * Stops playback and resets the playback position to the beginning.
     *
     * Unlike [pause], the current position is not saved; calling [play] after [stop]
     * will start from the beginning of the MIDI file.
     */
    fun stop()

    /**
     * Pauses playback at the current position.
     *
     * The current tick position is saved internally. Calling [play] after [pause]
     * will resume from the paused position.
     */
    fun pause()

    /**
     * Seeks to the given tick position in the MIDI file.
     *
     * Can be called while playing or stopped.
     *
     * @param tick Target position in MIDI ticks.
     */
    fun seekTo(tick: Long)

    /**
     * Whether the player is currently playing.
     *
     * Returns `false` when stopped, paused, or after playback has completed.
     */
    val isPlaying: Boolean

    /**
     * The total duration of the loaded MIDI file in ticks.
     *
     * Returns 0 if the duration cannot be determined.
     */
    val durationTicks: Long

    /**
     * Releases all native FluidSynth resources (player, audio driver, synth engine, settings).
     *
     * Must be called when the player is no longer needed to avoid native memory leaks.
     * The instance must not be used after calling [close].
     */
    fun close()
}
