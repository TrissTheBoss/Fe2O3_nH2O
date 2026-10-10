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

## Owner-reported hardware backend observations — 2026-10-08

The owner supplied separate Windows x86_64 Prism Launcher 10.0.5 logs for
Minecraft 26.2 with `fe2o3-nh2o-0.1.0-alpha.1`. The runs use Fabric Loader
0.19.5, Java 25.0.1, an AMD Radeon RX 6800 XT and driver 26.8.1, 854x480,
FPS Display 5.1.0+26.2, and a superflat test world. Earlier messages describe
render distance 15 and simulation distance 10 for the test. Treat FPS values as
the owner's in-game chat reports, not measurements extracted from an automated
benchmark.

| Minecraft graphics backend | Earlier owner-reported FPS | Latest owner-reported FPS | Fe2O3 wgpu adapter in latest log |
| --- | --- | --- | --- |
| OpenGL | At least 3,300 FPS (reported minimum) | About 3,500–3,900 FPS | AMD Radeon RX 6800 XT, GL backend |
| Vulkan | 2,100–2,200 FPS (reported min/max); another message says no more than 2,300 | Same as before, about 2,100–2,300 FPS | AMD Radeon RX 6800 XT, Vulkan backend |

Both latest logs show the corresponding Minecraft backend preference
(OpenGL -> `gl`, Vulkan -> `vulkan`), successful wgpu adapter initialization
on the RX 6800 XT, and generation of a 16x512, two-level mip chain. This is
physical-hardware evidence that the preference alignment works on this GPU for
both APIs across separate launches. The launches do not test changing APIs
in-place; Minecraft applies its graphics choice on restart.

The latest OpenGL reading is approximate chat text (“3500 to 3900~”), while the
Vulkan figures are the owner's approximate range and earlier min/max chat
entries. They are not raw samples or frame-time percentiles. There is no
enabled/disabled comparison, controlled benchmark, screenshot, or long stress
test here. Since world drawing remains vanilla, the reported frame rates do not
isolate any effect of mipmap generation and do not establish an FPS improvement.
These logs also do not establish full resource-pack or visual parity.

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

## Blaze3D-aware wgpu backend preference — 2026-10-08

PR #9 code head `3b965f84366d625ea63f203e7bb7ed9bb25826d8` passed the complete
workflow in [GitHub Actions run 37832107479](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37832107479).
All four native targets passed formatting, Rust tests, Clippy with warnings
denied, and release compilation: Ubuntu x86_64, Windows x86_64, macOS arm64,
and macOS Intel. Seven JVM tests passed, including backend-name mapping,
mandatory software-Vulkan execution, and exact mip comparisons against
Minecraft's MEAN implementation. Packaged JAR/native checksum validation passed.

The production client initialized Minecraft on OpenGL and logged the detected
Blaze3D backend as OpenGL with Fe2O3 preference `gl`. The CI environment could
not load wgpu's GL drivers, so the native stage logged its fallback and created
a Vulkan device on llvmpipe/Mesa 25.2.8. It generated the first Minecraft mip
chain, and the required live scene and resource-reload smoke checks passed.
The run's [production JAR artifact](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37832107479/artifacts/11573263702)
expires 2026-11-07.

This verifies the startup hook, preference transfer, unavailable-preferred-
backend fallback and existing live client path on software rendering. It does
not verify wgpu GL computation, Minecraft's Vulkan presentation in this run,
physical-GPU behavior, in-place backend switching, a world-rendering pass,
visual parity or performance improvement. Hardware tests on the owner's RX
6800 XT remain necessary to determine whether wgpu GL is preferable there or
the fallback is selected.


## Blaze3D resource lifecycle smoke coverage — 2026-10-09

PR #12 head `dd1d5eff782f6677b19ffa58f24cccd3858061fa` passed the complete
workflow in [Actions run 37980116959](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37980116959).
All four native target jobs passed. The Fabric JAR build, mandatory software
Vulkan differential tests, packaged-native checks, and live Minecraft client
smoke test passed.

The client test observed an active Blaze3D generation, registered the same
resource twice to verify idempotence, and allocated a 4-byte `GpuBuffer` from
the active `GpuDevice`. It then simulated lifecycle detach and reattach using
the same active device object: the first buffer closed, the resource received a
new generation and allocated a fresh buffer, and unregister closed that buffer.
Existing live Mixin, scene, and resource-reload checks passed.

This verifies real handle allocation, cleanup, and simulated reinitialization on
CI software Vulkan. It does not exercise actual renderer-device replacement on
hardware, register a production rendering resource, share the wgpu compute
device, replace a vanilla world pass, or prove visual parity/performance
improvement. M2 remains in progress.


## Exact documentation-head verification — 2026-10-09

PR #12 documentation head `298458db44ab8ebb78fe380c93e379c3d965cc15`
passed all four native matrix jobs and the full `verify` job in
[Actions run 37980839098](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37980839098).
The first attempt's verify job was cancelled while the client smoke test was
running; a rerun of that job completed successfully. The completed attempt
passed software Vulkan installation, Fabric JAR build and vanilla differential
tests, packaged-native checksums, and the live Minecraft client test.

The lifecycle smoke behavior is as described above: it uses the active device
and simulates detach/reattach with the same device object. The successful run
does not add evidence for real device replacement or the outstanding M2 world
pass. The PR remains open and M2 remains in progress.


## Blaze3D callback failure isolation — 2026-10-09

PR #14 code head `d3d9272ec399256e98a341159fa8a0bd0dc5783e` passed all four
native jobs and the full verify job in
[Actions run 37988785889](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37988785889).
The live Minecraft smoke test deliberately caused one resource's initialization
callback to throw and verified its release callback ran. It also caused a
resource's release callback to throw during simulated device detach while a
healthy buffer resource was registered; the healthy resource still closed,
reinitialized on simulated reattach, and remained usable.

These are callback fault-containment checks on the active CI device. They do not
test actual renderer-device replacement, hardware device loss, a production
rendering resource, or a replaced vanilla world pass. M2 remains in progress.


## Assertion failure containment in Blaze3D callbacks — 2026-10-10

PR #14 head `9efa60dbc5c4de9b06363f3a51dfdaba7441d614` passed the full
four-platform and live client workflow in
[Actions run 38079582114](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/38079582114).
The client test raised an intentional `AssertionError` from resource
initialization; the lifecycle logged and removed that resource, invoked its
cleanup callback, then continued attaching healthy resources. A separate
intentional runtime exception during resource release also did not block cleanup
or reinitialization of the healthy GPU-buffer resource.

This verifies callback containment for `AssertionError`, runtime exceptions
and linkage failures. Fatal VM and thread errors are not swallowed. The test
still simulates reinitialization on the same device and does not verify actual
device replacement, a production render resource, or a replaced vanilla pass.
M2 remains in progress.

## Same-device lifecycle idempotence and backend candidate review — 2026-10-10

PR #14 head `22905a6cf0d29728cbbf99b02884b92b56899c8e` passed all jobs in
[Actions run 38081353775](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/38081353775):
four native matrix targets, mandatory software-Vulkan differential tests,
packaged JAR/native checksum validation, and the live Minecraft client smoke
test. The smoke test verified that invoking both renderer initialization
callbacks again with the same active Blaze3D device preserves the generation,
live GPU buffer, and callback counts. It also retained the initialization/release
failure-isolation and simulated detach/reattach checks.

This run validates same-device idempotence. It does not recreate Minecraft's
actual renderer device, use a production rendering resource, or replace a
vanilla draw pass. The backend adoption review is recorded in ADR-010:
wgpu-mc/Electrum remains a reference, not a pinned dependency, while its
README describes a compatibility rewrite and no releases are published. The
repository's top-level license is identified as MPL-2.0; bundled/transitive
license review remains open. M2 remains incomplete.
