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
a passing client test. The workflow now explicitly supplies the remapped
`gametest` JAR and requires a completion marker plus both screenshots.

## Outstanding gates

- Live Mixin-enabled Minecraft client, scene and resource reload: pending after fixing test-mod classpath wiring; CI must prove execution.
- Four-native JAR payload/checksum verification and generated notices: pending.
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
