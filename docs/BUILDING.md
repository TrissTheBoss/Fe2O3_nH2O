# Building and troubleshooting

## Prerequisites and reproducibility

JDK 25; Gradle **9.5.1**; Rust **1.90.0** (rustup reads rust-toolchain.toml);
Python 3; a C/C++ linker supported by Rust. Linux natives use an Ubuntu 24.04
baseline. Install graphics drivers supporting one of wgpu's native backends.
Do not run Windows native builds from a Unix cross-linker without a tested target.

```sh
cargo test --manifest-path native/Cargo.toml
cargo clippy --manifest-path native/Cargo.toml --all-targets -- -D warnings
cargo build --manifest-path native/Cargo.toml --release
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
An ephemeral interface inspection workflow documents the initial upstream
investigation. It must not be used to publish game binaries or decompiled sources.
No Actions job commits to the repository or uses personal access tokens.

