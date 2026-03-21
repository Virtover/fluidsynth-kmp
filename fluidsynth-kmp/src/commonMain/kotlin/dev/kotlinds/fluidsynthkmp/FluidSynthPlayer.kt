package dev.kotlinds.fluidsynthkmp

/**
 * A real-time FluidSynth synthesizer.
 *
 * Manages the full FluidSynth lifecycle: settings, synth engine, and audio driver.
 * Audio output starts immediately on construction via the platform's default audio driver.
 *
 * @param config Audio configuration (sample rate, interpolation quality, buffer settings).
 */
expect class FluidSynthPlayer(config: AudioConfig = AudioConfig()) {

    /**
     * Loads a SoundFont (.sf2) file from the given file path.
     *
     * @param path Absolute path to the SoundFont file.
     * @return The SoundFont ID (≥ 0) on success, or -1 on failure.
     */
    fun loadSoundFont(path: String): Int

    /**
     * Sends a MIDI note-on event.
     *
     * @param channel MIDI channel (0–15).
     * @param key MIDI note number (0–127). Middle C = 60.
     * @param velocity Note velocity (0–127). 0 is equivalent to note-off.
     */
    fun noteOn(channel: Int, key: Int, velocity: Int)

    /**
     * Sends a MIDI note-off event.
     *
     * @param channel MIDI channel (0–15).
     * @param key MIDI note number (0–127).
     */
    fun noteOff(channel: Int, key: Int)

    /**
     * Changes the active program (instrument) on a MIDI channel.
     *
     * @param channel MIDI channel (0–15).
     * @param program General MIDI program number (0–127). See General MIDI specification for instrument list.
     */
    fun programChange(channel: Int, program: Int)

    /**
     * Sets the master output gain.
     *
     * @param gain Gain factor. Typical range: 0.0 (silent) to 1.0 (full). Can exceed 1.0, max ~10.0.
     */
    fun setGain(gain: Float)

    /**
     * Changes the interpolation method at runtime, applying to all MIDI channels.
     *
     * @param interpolation New interpolation quality to use.
     */
    fun setInterpolation(interpolation: Int)

    /**
     * Configures the reverb effect for all effect groups.
     *
     * Reverb simulates room acoustics. All parameters apply to every effect group (fx_group = -1).
     *
     * @param roomSize Room size factor (0.0–1.0). Larger values produce a longer reverb tail.
     * @param damping High-frequency damping (0.0–1.0). Higher values absorb more high frequencies.
     * @param width Stereo width of the reverb (0.0–100.0).
     * @param level Output level of the reverb effect (0.0–1.0).
     */
    fun setReverb(roomSize: Double, damping: Double, width: Double, level: Double)

    /**
     * Configures the chorus effect for all effect groups.
     *
     * Chorus thickens the sound by mixing slightly delayed and pitch-shifted copies.
     * All parameters apply to every effect group (fx_group = -1).
     *
     * @param voiceCount Number of chorus voices (0–99).
     * @param level Chorus output level (0.0–10.0).
     * @param speed Modulation speed in Hz (0.1–5.0).
     * @param depth Modulation depth in milliseconds (0.0–256.0).
     */
    fun setChorus(voiceCount: Int, level: Double, speed: Double, depth: Double)

    /**
     * Renders audio to a stereo interleaved float buffer without using the audio driver.
     *
     * Useful for offline rendering, recording, or custom audio pipelines.
     * Note: the audio driver is still created on construction; this function renders
     * an additional buffer from the current synth state.
     *
     * @param frames Number of audio frames to render.
     * @return Interleaved stereo [FloatArray] of size `frames * 2` (L, R, L, R, …).
     */
    fun renderFloat(frames: Int): FloatArray

    /**
     * Releases all native FluidSynth resources (audio driver, synth engine, settings).
     *
     * Must be called when the player is no longer needed to avoid native memory leaks.
     * The instance must not be used after calling [close].
     */
    fun close()
}
