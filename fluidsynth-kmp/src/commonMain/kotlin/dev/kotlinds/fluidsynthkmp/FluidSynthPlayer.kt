package dev.kotlinds.fluidsynthkmp

/**
 * A real-time FluidSynth synthesizer player.
 * Manages a settings + synth + audio driver lifecycle.
 */
expect class FluidSynthPlayer(sampleRate: Int = 44100) {
    fun loadSoundFont(path: String): Int
    fun noteOn(channel: Int, key: Int, velocity: Int)
    fun noteOff(channel: Int, key: Int)
    fun programChange(channel: Int, program: Int)
    fun setGain(gain: Float)
    fun setReverb(roomSize: Double, damping: Double, width: Double, level: Double)
    fun setChorus(voiceCount: Int, level: Double, speed: Double, depth: Double)
    fun renderFloat(frames: Int): FloatArray
    fun close()
}
