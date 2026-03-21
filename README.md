# fluidsynth-kmp

Kotlin Multiplatform wrapper for [FluidSynth](https://www.fluidsynth.org/), a real-time SF2/MIDI software synthesizer.
Supports Android, iOS, macOS, Linux, Windows, and JVM/Desktop from a single Kotlin API.

[![License](https://img.shields.io/github/license/kotlinds/fluidsynth-kmp)](LICENSE)
[![Maven Central Version](https://img.shields.io/maven-central/v/dev.kotlinds/fluidsynth-kmp)](https://klibs.io/project/kotlinds/fluidsynth-kmp)
[![Issues](https://img.shields.io/github/issues/kotlinds/fluidsynth-kmp)]()
[![Pull Requests](https://img.shields.io/github/issues-pr/kotlinds/fluidsynth-kmp)]()

## What's included

| Component                           | Version |
|-------------------------------------|---------|
| FluidSynth (Android prebuilt `.so`) | 2.5.3   |
| FluidSynth (iOS XCFramework)        | 2.5.2   |
| JNA (JVM/Desktop binding)           | 5.15.0  |
| Kotlin                              | 2.3.0   |

### Supported targets

| Platform                                 | Integration                             |
|------------------------------------------|-----------------------------------------|
| Android (arm64-v8a, armeabi-v7a, x86_64) | JNI + prebuilt `.so` (bundled)          |
| iOS device (arm64)                       | cinterop + XCFramework (bundled)        |
| iOS simulator (arm64 + x86_64)           | cinterop + XCFramework (bundled)        |
| macOS (arm64, x64)                       | cinterop + system FluidSynth (Homebrew) |
| Linux (x64, arm64)                       | cinterop + system FluidSynth            |
| Windows (x64)                            | cinterop + system FluidSynth            |
| JVM/Desktop                              | JNA + system FluidSynth                 |

Android and iOS ship with FluidSynth bundled — no extra installation needed. Desktop platforms require FluidSynth
installed on the system (see Platform setup below).

## Installation

Add the dependency from Maven Central:

```kotlin
// build.gradle.kts
dependencies {
    implementation("dev.kotlinds:fluidsynth-kmp:1.1.0")
}
```

## Platform setup

### Android

No extra setup — FluidSynth `.so` libraries are bundled for all supported ABIs.

### iOS

No extra setup — FluidSynth is bundled as an XCFramework.

### macOS

```bash
brew install fluidsynth
```

### Linux

```bash
sudo apt install libfluidsynth-dev   # Debian/Ubuntu
sudo dnf install fluidsynth-devel    # Fedora
```

### Windows

Install FluidSynth and ensure `fluidsynth.dll` is on your `PATH`, or place it next to your application executable.
Prebuilt Windows binaries are available from
the [FluidSynth releases page](https://github.com/FluidSynth/fluidsynth/releases).

## Usage

### Audio configuration — `AudioConfig`

Both `FluidSynthPlayer` and `MidiFilePlayer` accept an optional `AudioConfig` to control audio quality and latency.

```kotlin
val config = AudioConfig(
    sampleRate = 44100,           // output sample rate in Hz
    interpolation = Interpolation.HIGH, // resampling quality (FAST, NORMAL, HIGH)
    periodSize = 64,              // audio frames per buffer period (lower = less latency)
    periods = 2,                  // number of buffer periods
)
```

| Parameter       | Default              | Description                                            |
|-----------------|----------------------|--------------------------------------------------------|
| `sampleRate`    | `44100`              | Output sample rate in Hz.                              |
| `interpolation` | `Interpolation.HIGH` | Resampling quality. `HIGH` gives best audio fidelity.  |
| `periodSize`    | `64`                 | Frames per buffer period. Lower values reduce latency. |
| `periods`       | `2`                  | Number of buffer periods.                              |

**Interpolation modes:**

| Value                  | FluidSynth method | Description                            |
|------------------------|-------------------|----------------------------------------|
| `Interpolation.FAST`   | Nearest-neighbour | Lowest CPU usage, lowest quality.      |
| `Interpolation.NORMAL` | 4th-order cubic   | Balanced quality and CPU usage.        |
| `Interpolation.HIGH`   | 7th-order sinc    | Best audio quality, highest CPU usage. |

### Real-time synthesis — `FluidSynthPlayer`

Use `FluidSynthPlayer` to load a SoundFont and play notes in real time. The audio driver starts automatically on
construction.

```kotlin
val player = FluidSynthPlayer(AudioConfig(sampleRate = 48000, interpolation = Interpolation.HIGH))

// Load a SoundFont (.sf2) from a file path
val sfontId = player.loadSoundFont("/path/to/soundfont.sf2")

// Select a program (instrument) on channel 0
// General MIDI program 0 = Acoustic Grand Piano
player.programChange(channel = 0, program = 0)

// Play middle C (MIDI note 60) at velocity 100
player.noteOn(channel = 0, key = 60, velocity = 100)

// ... later, release the note
player.noteOff(channel = 0, key = 60)

// Adjust master volume (0.0 to 10.0, default 0.2)
player.setGain(0.5f)

// Change interpolation quality at runtime
player.setInterpolation(Interpolation.FAST)

// Configure reverb (roomSize, damping, width, level)
player.setReverb(roomSize = 0.6, damping = 0.5, width = 0.5, level = 0.3)

// Configure chorus (voiceCount, level, speed, depth)
player.setChorus(voiceCount = 3, level = 2.0, speed = 0.3, depth = 8.0)

// Render audio offline (interleaved stereo, frames * 2 floats)
val buffer = player.renderFloat(frames = 1024)

// Always close when done to free native resources
player.close()
```

### MIDI file playback — `MidiFilePlayer`

Use `MidiFilePlayer` to play a complete `.mid` file through a SoundFont.

```kotlin
val player = MidiFilePlayer(
    soundFontPath = "/path/to/soundfont.sf2",
    midiPath = "/path/to/song.mid",
    config = AudioConfig(sampleRate = 44100, interpolation = Interpolation.HIGH),
)

// Play with a completion handler
player.play {
    println("Playback finished")
    player.close()
}

// Pause and resume
player.pause()
player.play()  // resumes from where it was paused

// Seek to a position
player.seekTo(1000L)

println("Duration: ${player.durationTicks} ticks")
println("Playing: ${player.isPlaying}")
```

### Android — loading files from assets

On Android, copy your SoundFont and MIDI files to a location accessible by path (e.g. the app's files directory) before
passing the path to the library:

```kotlin
// In your Activity or ViewModel
fun copyAssetToFile(context: Context, assetName: String): String {
    val file = File(context.filesDir, assetName)
    if (!file.exists()) {
        context.assets.open(assetName).use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
    }
    return file.absolutePath
}

val sfPath = copyAssetToFile(context, "GeneralUser.sf2")
val midiPath = copyAssetToFile(context, "song.mid")

val player = MidiFilePlayer(sfPath, midiPath)
player.play()
```

## API reference

### `AudioConfig`

| Parameter       | Type  | Default              | Description                       |
|-----------------|-------|----------------------|-----------------------------------|
| `sampleRate`    | `Int` | `44100`              | Output sample rate in Hz.         |
| `interpolation` | `Int` | `Interpolation.HIGH` | Resampling interpolation quality. |
| `periodSize`    | `Int` | `64`                 | Audio frames per buffer period.   |
| `periods`       | `Int` | `2`                  | Number of buffer periods.         |

### `FluidSynthPlayer`

| Method                                       | Description                                                          |
|----------------------------------------------|----------------------------------------------------------------------|
| `loadSoundFont(path: String): Int`           | Loads a SoundFont (.sf2) file. Returns sfont ID ≥ 0, or -1 on error. |
| `noteOn(channel, key, velocity)`             | Sends a MIDI note-on event.                                          |
| `noteOff(channel, key)`                      | Sends a MIDI note-off event.                                         |
| `programChange(channel, program)`            | Selects a General MIDI instrument (0–127) on a channel.              |
| `setGain(gain: Float)`                       | Sets master output gain. Typical range: 0.0–1.0.                     |
| `setInterpolation(interpolation: Int)`       | Changes resampling quality at runtime (all channels).                |
| `setReverb(roomSize, damping, width, level)` | Configures the reverb effect (all Double).                           |
| `setChorus(voiceCount, level, speed, depth)` | Configures the chorus effect.                                        |
| `renderFloat(frames: Int): FloatArray`       | Renders `frames` audio frames to an interleaved stereo float buffer. |
| `close()`                                    | Releases all native resources. Must be called when done.             |

### `MidiFilePlayer`

| Method / Property                        | Description                                                                                        |
|------------------------------------------|----------------------------------------------------------------------------------------------------|
| `play(onComplete: (() -> Unit)? = null)` | Starts or resumes playback. Optional callback fires on a background thread when the song finishes. |
| `stop()`                                 | Stops playback and resets position to start.                                                       |
| `pause()`                                | Pauses playback, saving the current position.                                                      |
| `seekTo(tick: Long)`                     | Seeks to a tick position in the MIDI file.                                                         |
| `isPlaying: Boolean`                     | `true` while actively playing (false when paused, stopped, or done).                               |
| `durationTicks: Long`                    | Total duration of the MIDI file in ticks.                                                          |
| `close()`                                | Releases all native resources. Must be called when done.                                           |

## License

The Kotlin wrapper code in this library is licensed under the **Apache License 2.0**.

The bundled **FluidSynth** native libraries are licensed under the **GNU Lesser General Public License v2.1 (LGPL-2.1)
**. On Android and JVM/Desktop, FluidSynth is dynamically linked, which is LGPL-compliant. On iOS, the XCFramework is
dynamically linked as an embedded framework.

See [LICENSE](LICENSE) and the [FluidSynth license](https://github.com/FluidSynth/fluidsynth/blob/master/LICENSE) for
details.
