# Building and troubleshooting

## Prerequisites and reproducibility

JDK 25; Gradle **9.5.1**; Rust **1.90.0** (rustup reads rust-toolchain.toml);
Python 3; a C/C++ linker supported by Rust. Linux natives use an Ubuntu 24.04
baseline. Install graphics drivers supporting one of wgpu's native backends.
Do not run Windows native builds from a Unix cross-linker without a tested target.

```sh
cargo test --locked --manifest-path native/Cargo.toml
cargo clippy --locked --manifest-path native/Cargo.toml --all-targets -- -D warnings
cargo build --locked --manifest-path native/Cargo.toml --release
python tools/stage_native.py
gradle build
```

Run `FE2O3_REQUIRE_GPU=true gradle test` on Linux/macOS to prohibit skipped GPU
tests. PowerShell: set `$env:FE2O3_REQUIRE_GPU='true'` first. For Linux headless
testing install Mesa Vulkan drivers and provide a valid XDG_RUNTIME_DIR. No
display surface is required by the compute pipeline.

Rust checks precede the release build. Staging copies the local binary and digest
under `build/native/<platform>/`; Gradle includes that tree in the JAR. A plain
Gradle build without staging can compile Java but produces no accelerated native
for that platform. CI combines four native artifacts before building the JAR.
Always inspect artifact contents and reports before distribution.

## Troubleshooting

| Symptom | Action |
| --- | --- |
| No first-mip message | Source may be below the threshold or use a specialized strategy; try minPixels=4 for diagnosis |
| No bundled native | Check OS/architecture against the packaged targets; rebuild for that target or use vanilla fallback |
| Native load error | Check driver, OS ABI/glibc baseline and executable permission on the JVM temp directory |
| Adapter error | Check native graphics drivers; a browser WebGPU flag has no effect |
| Pixel comparison failure | Do not weaken assertions; inspect input ordering, tables, strategy and generated levels |
| Mixin signature failure | Verify exact game version and other renderer mods; re-inspect upstream before changing the target |
| GPU timeout | Stage disables itself; collect logs, device info and image dimensions for diagnosis |

## Actions workflow

Build jobs have read-only repository permissions and timeouts. Native outputs
are intermediate artifacts retained for 14 days; verified JARs for 30 days.
The historical interface inspection runs document the initial upstream investigation;
their temporary workflow was removed. Never publish game binaries or decompiled sources.
No Actions job commits to the repository or uses personal access tokens.

The production client test runs with `gradle runProductionClientGameTest` using
Fabric API 0.161.0+26.2 as a test harness, Xvfb on Linux and the built JAR. The
test mod is a separate source set, packaged as a dedicated JAR for the production
client run, and is not bundled in the production JAR.
The upstream-documented network-synchronizer workaround is enabled for CI.
It creates a disposable singleplayer fixture and captures the scene and reload.

CI uses community `cargo-about` 0.9.2 to generate dependency license texts at
`natives/licenses/third-party.html` in the JAR. To include them in a local release:

```sh
cargo install --locked --features cli cargo-about --version 0.9.2
mkdir -p build/native/licenses
cargo about generate --locked --fail --manifest-path native/Cargo.toml native/about.hbs > build/native/licenses/third-party.html
gradle build
```

`python tools/verify_jar.py` checks a combined four-platform JAR; it intentionally
fails for a local single-platform artifact. Production archives are validated
before publishing and receive a SHA-256 sidecar. See VERIFICATION.md for executed results.

