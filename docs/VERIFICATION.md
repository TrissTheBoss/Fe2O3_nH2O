# Executed verification

## Initial compiled milestone — 2026-10-08

Commit: `2c650f96c538c7956daf9fc1337a23601f8a330e`

[GitHub Actions run 37795235734](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37795235734)
completed successfully. Four native targets passed Rust unit tests, Clippy with
warnings denied, and release compilation: Ubuntu 24.04 x86_64, Windows 2022
x86_64, macOS 15 arm64 and macOS 15 Intel. Gradle 9.5.1/Loom 1.17.21 built the
Minecraft 26.2 JAR using Java 25.

All five JVM tests passed with GPU execution mandatory, none skipped:
platform mapping; 100,000 color-table comparisons; 24 complete GPU chains
against actual Minecraft output; specialized strategy/supplied-level fallback;
and malformed JNI inputs followed by a valid request.

Adapter: **llvmpipe (LLVM 20.1.2, 256 bits)**, Mesa **25.2.8**, backend **Vulkan**,
device type **CPU**. This verifies native WebGPU computation on software Vulkan,
not a hardware performance result. First live GPU request in that JVM was a
2x2 source with one generated level.

A later client-launch attempt (Actions run 37797119698) started Minecraft and
initialized the Mixin/native library, but the separate client-test mod was not
on the production run classpath. It produced no scene screenshots. This is a
false positive from the launch task exiting successfully; it is not counted as
a passing client test. The workflow now builds and supplies a dedicated
`gametest` JAR and requires a completion marker plus scene screenshots.

## Completed client and distribution verification — 2026-10-08

Commit: `5263a98e78f0609928a6f42c8a2ab507ff6cbcdc`

[GitHub Actions run 37803100663](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37803100663)
completed successfully. Four native targets passed Rust tests, Clippy with
warnings denied, and release compilation. All five JVM tests passed with
mandatory GPU execution; the adapter was llvmpipe/Mesa 25.2.8 on Vulkan.

The production JAR passed metadata, isolation, four-native SHA-256, and bundled
license-notice checks. Its SHA-256 is
`ea8e7da9b86a9f9cd21b9d928df1b19ad39945dfa1c3200edd0a92671c358832`.
Download the verified production archive from the
[Actions artifact](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37803100663/artifacts/11561763142).

The production Minecraft client loaded the separate test mod and live Mixin.
The test checked a known mip pixel and GPU execution count, created a disposable
scene with full and partial blocks, glass, leaves, a pig, flame particles and a
noon sky, then reloaded resources and confirmed another GPU mip chain. The
completion marker and both non-empty screenshots were required by CI:
`0000_terrain-partial-blocks-entity-particles-sky.png` and
`0001_after-resource-reload.png`. These are smoke captures, not parity baselines.

## Expanded fallback and strategy verification — 2026-10-08

Commit: `e9abfea03abd719ce055bf2a1f48251bb0a2651b`

[GitHub Actions run 37805765573](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37805765573)
completed successfully. All four native targets passed Rust tests, Clippy with
warnings denied and release compilation. Linux CI ran the six JVM tests with
GPU execution mandatory on llvmpipe/Mesa 25.2.8 using Vulkan, including the new
opaque AUTO differential test against Minecraft's actual MipmapGenerator.
Packaged native payload and JAR metadata checks passed.

The live Fabric client/Mixin test also passed. In addition to the existing
MEAN GPU output, world scene and reload checks, it verified that CUTOUT uses
vanilla and does not increment the GPU completion counter. The separate
`verification-reports` artifact contains the completion marker and required
non-empty screenshots. The production JAR is available from the
[run 13 distribution artifact](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37805765573/artifacts/11562373419).

The run's native software adapter is evidence for WebGPU execution and exact
mipmap output, not physical GPU performance. Full resource-pack visual parity,
Windows/macOS GPU execution, alternate Minecraft presentation backends, and
end-to-end performance/memory benchmarks remain unverified.

## Mipmap benchmark harness — 2026-10-08

PR head `9c7c6b3f404229bb0d6c4acebfa08ff4ad8179f5` passed all four native
matrix jobs and the `verify` job in
[Actions run 37824336848](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37824336848).
The opt-in benchmark class compiled and the default CI test suite completed;
the timing comparison itself was not requested, so no benchmark measurements
were collected. The machine-specific performance and threshold gates remain
pending.

## Owner-reported Windows hardware smoke run — 2026-10-08

The project owner reports flawless gameplay and very high FPS using
`fe2o3-nh2o-0.1.0-alpha.1` in a Windows x86_64 Prism Launcher instance with
Minecraft 26.2, Fabric Loader 0.19.5, Java 25.0.1, and an AMD Radeon RX 6800 XT.
The attached launcher log records Minecraft's graphics backend as OpenGL and
the Rust/WebGPU mipmap adapter as that Radeon GPU using Vulkan and AMD driver
26.8.1. It records initialization of the mipmap stage, generation of a
`16x512`, two-level mip chain, and entry into a new single-player world.
The log contains no numeric FPS samples, disabled-mod comparison, extended
stress duration, resource-pack test, screenshot, or Graphics API change/recovery
test. The reported high FPS is therefore qualitative owner feedback, not a
controlled measurement or demonstrated speedup attributable to this
load-time mipmap stage. This run is evidence that the released JAR starts and
executes its native WebGPU mipmap path on this Windows GPU while Minecraft uses
OpenGL.

## Outstanding gates

- Windows/macOS GPU execution, physical GPUs beyond the owner run above, and Minecraft Vulkan presentation: pending.
- Full resource-pack and visual parity matrix: pending.
- Controlled end-to-end performance and memory benchmarks: pending; no measured speedup claim.

## Upstream interface evidence

The initial inspection runs verified 26.2's mipmap signature, specialized
strategies, NativeImage methods, integer ARGB tables and Fabric version metadata:
[first inspection](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37793070353)
and [color inspection](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37793203743).
The temporary inspection workflow is removed from the final branch; its history
and the architectural conclusions remain. No game binaries or sources are tracked.
