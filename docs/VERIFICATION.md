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

## Owner-reported hardware backend comparison — 2026-10-08

The project owner supplied two Windows x86_64 Prism Launcher 10.0.5 logs for
the same Minecraft 26.2 instance and `fe2o3-nh2o-0.1.0-alpha.1` JAR. Both use
Fabric Loader 0.19.5, Java 25, an AMD Radeon RX 6800 XT, AMD driver 26.8.1,
854x480 window size, FPS Display 5.1.0+26.2, and a superflat test world. The
owner states the world and settings were the same, with render distance 15
and simulation distance 10.

| Minecraft graphics backend | Owner-reported FPS Display readings | Fe2O3 wgpu backend | Startup/world evidence |
| --- | --- | --- | --- |
| OpenGL | At least 3,300 FPS (reported minimum) | Vulkan on RX 6800 XT | Minecraft initialized; GPU mip stage generated a 16x512, two-level chain; entered test world |
| Vulkan | 2,100–2,200 FPS (reported min/max); later message says it did not exceed 2,300 | Vulkan on RX 6800 XT | Minecraft initialized; GPU mip stage generated a 16x512, two-level chain; entered test world |

The logs record the owner typing these readings and conditions into the in-game
chat. They support that the released JAR starts under both Minecraft backends,
and that its independent WebGPU mipmap path runs on Vulkan in either case. They
also capture successful backend selection across separate launches. They do
not show in-place backend switching; that is not expected from this test.
The owner's qualitative report is that gameplay was flawless with very high
FPS. These observations do not provide raw time-series samples, frame-time
percentiles, a disabled-mod baseline, a screenshot or a long stress test. Since
Minecraft's world drawing remains vanilla, the readings characterize the full
test setup and do not isolate an FPS contribution from mipmap generation.

The two logs document real hardware and user-reported readings, not a controlled
performance benchmark or full visual/resource-pack parity test. No claim is made
that the mod itself causes the observed steady-state FPS.

## Outstanding gates

- Additional physical GPUs and Minecraft Vulkan presentation on other hardware: pending.
- Full resource-pack and visual parity matrix: pending.
- Controlled same-backend enabled/disabled performance and memory benchmarks: pending.

## Upstream interface evidence

The initial inspection runs verified 26.2's mipmap signature, specialized
strategies, NativeImage methods, integer ARGB tables and Fabric version metadata:
[first inspection](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37793070353)
and [color inspection](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37793203743).
The temporary inspection workflow is removed from the final branch; its history
and the architectural conclusions remain. No game binaries or sources are tracked.
