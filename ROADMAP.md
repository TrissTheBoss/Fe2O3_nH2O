# Roadmap

Fe2O3 is narrowing its scope to Minecraft texture mipmap optimization and a
Sodium addon integration. Status words describe evidence: **implemented** means
code exists, **verified** means the stated test passed, and **planned** means
work remains. No stage is complete based on compilation alone.

## M0 — Rust/wgpu mipmap prototype (implemented; CI verified)

- Fabric client adapter and Rust/wgpu compute stage for eligible MEAN mipmaps.
- Exact comparisons against Minecraft's implementation and vanilla fallback
  for unsupported requests.
- Bundled native libraries for four desktop targets and software-Vulkan CI.
- Owner-reported RX 6800 XT launch smoke checks on Minecraft OpenGL and Vulkan.
- This stage is standalone; it does not depend on Sodium or share its renderer.

The prototype is retained as a correctness baseline. Its FPS observations are
not evidence that mipmap generation improves steady-state rendering.

## M1 — Measure and optimize mipmap generation (in progress)

- Run the end-to-end vanilla versus native benchmark on representative hardware.
- Measure loading/reload time, CPU time, JNI copies, GPU dispatch/readback, and
  peak memory for realistic resource packs.
- Benchmark thresholds by image size and mipmap strategy; keep acceleration
  disabled for cases where transfer overhead erases the gain.
- Expand exact-output cases for odd dimensions, alpha, supplied mip levels,
  animated textures, and supported pack metadata.
- Choose defaults only after reproducible wins with no output differences.

**Acceptance:** published raw measurements and exact-output results on each
supported backend/device combination. A faster shader microbenchmark alone
does not pass this stage.

## M2 — Sodium addon integration (planned)

- Pin the target Minecraft and Sodium versions and inspect their supported
  addon/configuration/rendering APIs.
- Build a minimal addon integration with a documented Sodium API where one
  covers the desired mipmap work. Keep the Rust core independent of Sodium and
  confine version-specific hooks to a small Java adapter.
- If the mipmap path has no supported extension point, test a narrowly scoped,
  version-specific compatibility hook and document its failure/fallback limits
  before adopting it.
- Decide whether Sodium is a required runtime dependency and update loader
  metadata only after a working integration test.
- Verify coexistence with Sodium's own renderer and the specific addon/API
  combinations listed in the test matrix.

**Acceptance:** the production client proves the addon entry point runs with the
target Sodium build, exact mipmap output is maintained, and unsupported
requests safely use the supported fallback. There is no promise of
compatibility with every current or future addon.

## M3 — Vulkan integration (planned)

- Establish which target Minecraft/Sodium releases support Vulkan and through
  which renderer abstraction.
- Use the active backend and renderer-owned resource lifecycle; do not force
  Graphics API settings, create a competing window surface, or assume OpenGL
  handles are portable to Vulkan.
- Prove resource ownership, synchronization, reload, resize, shutdown, and
  device-loss behavior before adding a production Vulkan path.
- Keep OpenGL and vanilla fallback paths tested independently.

**Acceptance:** live-client tests on supported OpenGL and Vulkan configurations,
plus pixel-correct output and a reproducible benchmark on physical GPUs. A
wgpu Vulkan compute adapter alone does not establish Sodium Vulkan rendering
compatibility.

## M4 — Profiled Rust hot paths (planned; exploratory)

- Profile the Sodium-enabled client and identify a specific CPU-bound operation
  that is both measurable and safe to integrate.
- Prefer stable extension points. Do not fork or replace Sodium internals
  without a clear integration point, pinned version, license review, and
  evidence that the operation can be offloaded without changing semantics.
- Implement a clean, bounded Rust component behind a narrow Java boundary only
  after a representative benchmark shows potential benefit.
- Compare output/behavior with the Java reference and measure JNI, copying,
  scheduling, allocation, and end-to-end frame or load costs.

**Acceptance:** a named hotspot, repeatable baseline, exact or explicitly
specified behavior tests, and a statistically defensible end-to-end improvement.
Rewriting Sodium code in Rust is a research goal, not a current capability or
a promise to ship a replacement for Sodium's renderer.

## M5 — Compatibility and release qualification (planned)

- Maintain a matrix of Minecraft, Java, Fabric Loader, Sodium, graphics backend,
  GPU/driver, resource pack, and tested addon combinations.
- Report exact tested combinations and known conflicts; never infer universal
  addon compatibility from one successful launch.
- Verify failure recovery, world changes, reloads, resize, fullscreen,
  renderer recreation, and game shutdown.
- Publish checksums, native provenance, dependencies, and benchmark evidence.

## Explicitly out of scope

- Replacing the full Minecraft or Sodium world renderer.
- Adding a minimap/gameplay map feature.
- Claiming that all Sodium addons work together without testing.
- Claiming Vulkan support from wgpu adapter selection alone.
- Reporting a performance gain from uncontrolled FPS observations.
