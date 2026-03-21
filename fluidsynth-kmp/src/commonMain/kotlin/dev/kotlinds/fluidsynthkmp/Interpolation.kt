package dev.kotlinds.fluidsynthkmp

/**
 * Interpolation quality used by the FluidSynth synthesizer.
 *
 * Higher quality modes improve audio fidelity (especially for pitch-shifted samples)
 * at the cost of increased CPU usage.
 */
object Interpolation {
    /**
     * Nearest-neighbour interpolation. Lowest CPU usage, lowest quality.
     */
    const val FAST = 1

    /**
     * 4th-order (cubic) interpolation. Balanced quality and CPU usage.
     */
    const val NORMAL = 4

    /**
     * 7th-order sinc interpolation. Best audio quality, highest CPU usage.
     */
    const val HIGH = 7
}
