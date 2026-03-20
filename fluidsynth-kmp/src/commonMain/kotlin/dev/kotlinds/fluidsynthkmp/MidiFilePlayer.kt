package dev.kotlinds.fluidsynthkmp

/**
 * A MIDI file player backed by FluidSynth.
 * Loads a SoundFont and MIDI file, plays back through the audio driver.
 */
expect class MidiFilePlayer(soundFontPath: String, midiPath: String, sampleRate: Int = 44100) {
    fun play()
    fun stop()
    fun pause()
    fun seekTo(tick: Long)
    val isPlaying: Boolean
    val durationTicks: Long
    fun close()
}
