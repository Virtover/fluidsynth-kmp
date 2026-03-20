# fluidsynth-kmp

Kotlin Multiplatform wrapper for [FluidSynth](https://www.fluidsynth.org/) — a real-time SF2/MIDI software synthesizer.
Supports Android, iOS, macOS, Linux, Windows, and JVM/Desktop from a single Kotlin API.

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
    implementation("dev.kotlinds:fluidsynth-kmp:1.0.0")
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

### Real-time synthesis — `FluidSynthPlayer`

Use `FluidSynthPlayer` to load a SoundFont and play notes in real time. The audio driver starts automatically on
construction.

```kotlin
val player = FluidSynthPlayer(sampleRate = 44100)

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

// Always close when done to free native resources
player.close()
```

### MIDI file playback — `MidiFilePlayer`

Use `MidiFilePlayer` to play a complete `.mid` file through a SoundFont.

```kotlin
val player = MidiFilePlayer(
    soundFontPath = "/path/to/soundfont.sf2",
    midiPath = "/path/to/song.mid",
    sampleRate = 44100
)

player.play()

// Check playback state
println(player.isPlaying) // true

// Stop playback
player.stop()

// Always close when done
player.close()
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

### `FluidSynthPlayer`

```kotlin
class FluidSynthPlayer(sampleRate: Int = 44100) {
    // Load a SoundFont file. Returns the SoundFont ID (≥ 0) or -1 on error.
    fun loadSoundFont(path: String): Int

    // Send a MIDI note-on event. velocity 0–127.
    fun noteOn(channel: Int, key: Int, velocity: Int)

    // Send a MIDI note-off event.
    fun noteOff(channel: Int, key: Int)

    // Change the active program (instrument) on a channel. program 0–127.
    fun programChange(channel: Int, program: Int)

    // Set the master gain. Typical range: 0.0–1.0, max ~10.0.
    fun setGain(gain: Float)

    // Release all native resources. Must be called when done.
    fun close()
}
```

### `MidiFilePlayer`

```kotlin
class MidiFilePlayer(
    soundFontPath: String,
    midiPath: String,
    sampleRate: Int = 44100
) {
    fun play()
    fun stop()
    val isPlaying: Boolean
    fun close()
}
```

## License

The Kotlin wrapper code in this library is licensed under the **Apache License 2.0**.

The bundled **FluidSynth** native libraries are licensed under the **GNU Lesser General Public License v2.1 (LGPL-2.1)
**. On Android and JVM/Desktop, FluidSynth is dynamically linked, which is LGPL-compliant. On iOS, the XCFramework is
dynamically linked as an embedded framework.

See [LICENSE](LICENSE) and the [FluidSynth license](https://github.com/FluidSynth/fluidsynth/blob/master/LICENSE) for
details.
