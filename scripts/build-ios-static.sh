#!/usr/bin/env bash
# Build static FluidSynth libraries for iOS (arm64 device + arm64/x86_64 simulator).
# Outputs:
#   libs/ios-static/arm64/libfluidsynth.a
#   libs/ios-static/arm64_x86_64-simulator/libfluidsynth.a
#
# Prerequisites: Xcode Command Line Tools, CMake

set -euo pipefail

FLUIDSYNTH_VERSION="2.5.2"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
STATIC_DIR="$ROOT_DIR/libs/ios-static"
BUILD_BASE="/tmp/fluidsynth-static-build"
SRC_DIR="/tmp/fluidsynth-src-$FLUIDSYNTH_VERSION"

# ── Download source ──────────────────────────────────────────────────────────
if [ ! -d "$SRC_DIR" ]; then
  echo "Downloading FluidSynth $FLUIDSYNTH_VERSION source..."
  curl -fsSL "https://github.com/FluidSynth/fluidsynth/archive/refs/tags/v$FLUIDSYNTH_VERSION.tar.gz" \
    | tar -xz -C /tmp
  mv "/tmp/fluidsynth-$FLUIDSYNTH_VERSION" "$SRC_DIR"
fi

# ── Shared CMake flags ────────────────────────────────────────────────────────
COMMON_CMAKE_FLAGS=(
  -DCMAKE_BUILD_TYPE=Release
  -DBUILD_SHARED_LIBS=OFF
  -DBUILD_TESTING=OFF
  # Use C++11 OSAL instead of GLib — no GLib dependency needed
  -Dosal=cpp11
  -Denable-framework=OFF
  -Denable-aufile=OFF
  -Denable-dbus=OFF
  -Denable-ipv6=OFF
  -Denable-jack=OFF
  -Denable-ladspa=OFF
  -Denable-midishare=OFF
  -Denable-opensles=OFF
  -Denable-oboe=OFF
  -Denable-oss=OFF
  -Denable-pulseaudio=OFF
  -Denable-readline=OFF
  -Denable-sdl2=OFF
  -Denable-systemd=OFF
  -Denable-threads=ON
  -Denable-coreaudio=ON
  -Denable-coremidi=ON
  -DCMAKE_OSX_DEPLOYMENT_TARGET=16.0
  # Prevent CMake from treating the CLI binary as an iOS app bundle
  -DCMAKE_MACOSX_BUNDLE=OFF
)

# ── Build for iOS device (arm64) ─────────────────────────────────────────────
build_device() {
  local OUT_DIR="$STATIC_DIR/arm64"
  local BUILD_DIR="$BUILD_BASE/ios-arm64"

  echo "Building for iOS device (arm64)..."
  SDK_PATH="$(xcrun --sdk iphoneos --show-sdk-path)"

  cmake -S "$SRC_DIR" -B "$BUILD_DIR" \
    "${COMMON_CMAKE_FLAGS[@]}" \
    -DCMAKE_SYSTEM_NAME=iOS \
    -DCMAKE_OSX_SYSROOT="$SDK_PATH" \
    -DCMAKE_OSX_ARCHITECTURES="arm64" \
    -DCMAKE_INSTALL_PREFIX="$BUILD_DIR/install"

  cmake --build "$BUILD_DIR" -j"$(sysctl -n hw.logicalcpu)"
  # Install only the library + headers (skip CLI binary — iOS doesn't allow plain executables)
  cmake --install "$BUILD_DIR" --component fluidsynth_runtime
  cmake --install "$BUILD_DIR" --component fluidsynth_development

  mkdir -p "$OUT_DIR"
  cp "$BUILD_DIR/install/lib/libfluidsynth.a" "$OUT_DIR/libfluidsynth.a"
  echo "  -> $OUT_DIR/libfluidsynth.a"
}

# ── Build for iOS Simulator (arm64 + x86_64, merged with lipo) ───────────────
build_simulator() {
  local OUT_DIR="$STATIC_DIR/arm64_x86_64-simulator"
  local SDK_PATH="$(xcrun --sdk iphonesimulator --show-sdk-path)"

  echo "Building for iOS Simulator arm64..."
  local BUILD_SIM_ARM="$BUILD_BASE/sim-arm64"
  cmake -S "$SRC_DIR" -B "$BUILD_SIM_ARM" \
    "${COMMON_CMAKE_FLAGS[@]}" \
    -DCMAKE_SYSTEM_NAME=iOS \
    -DCMAKE_OSX_SYSROOT="$SDK_PATH" \
    -DCMAKE_OSX_ARCHITECTURES="arm64" \
    -DCMAKE_INSTALL_PREFIX="$BUILD_SIM_ARM/install"
  cmake --build "$BUILD_SIM_ARM" -j"$(sysctl -n hw.logicalcpu)"
  cmake --install "$BUILD_SIM_ARM" --component fluidsynth_runtime
  cmake --install "$BUILD_SIM_ARM" --component fluidsynth_development

  echo "Building for iOS Simulator x86_64..."
  local BUILD_SIM_X86="$BUILD_BASE/sim-x86_64"
  cmake -S "$SRC_DIR" -B "$BUILD_SIM_X86" \
    "${COMMON_CMAKE_FLAGS[@]}" \
    -DCMAKE_SYSTEM_NAME=iOS \
    -DCMAKE_OSX_SYSROOT="$SDK_PATH" \
    -DCMAKE_OSX_ARCHITECTURES="x86_64" \
    -DCMAKE_INSTALL_PREFIX="$BUILD_SIM_X86/install"
  cmake --build "$BUILD_SIM_X86" -j"$(sysctl -n hw.logicalcpu)"
  cmake --install "$BUILD_SIM_X86" --component fluidsynth_runtime
  cmake --install "$BUILD_SIM_X86" --component fluidsynth_development

  echo "Merging arm64 + x86_64 simulator slices with lipo..."
  mkdir -p "$OUT_DIR"
  lipo -create \
    "$BUILD_SIM_ARM/install/lib/libfluidsynth.a" \
    "$BUILD_SIM_X86/install/lib/libfluidsynth.a" \
    -output "$OUT_DIR/libfluidsynth.a"
  echo "  -> $OUT_DIR/libfluidsynth.a"
}

build_device
build_simulator

echo ""
echo "Static libraries ready in $STATIC_DIR"
echo "  arm64/libfluidsynth.a               (iOS device)"
echo "  arm64_x86_64-simulator/libfluidsynth.a  (iOS simulator)"
