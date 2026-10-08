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

## Outstanding gates

- Windows/macOS GPU execution, physical GPUs and Minecraft Vulkan presentation: pending.
- Full resource-pack and visual parity matrix: pending.
- End-to-end performance and memory benchmarks: pending; no speedup claim.

## Upstream interface evidence

The initial inspection runs verified 26.2's mipmap signature, specialized
strategies, NativeImage methods, integer ARGB tables and Fabric version metadata:
[first inspection](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37793070353)
and [color inspection](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37793203743).
The temporary inspection workflow is removed from the final branch; its history
and the architectural conclusions remain. No game binaries or sources are tracked.
