# Roadmap

Status vocabulary: **implemented** means code exists; **verified** requires a
linked test run; **planned** means it does not exist. No percentage estimates.

## M0 — first native rendering stage (implemented; CI verified)

- Rust/wgpu compute pipeline generating standard mip chains in one submission.
- Fabric 26.2 integration through a narrow MipmapGenerator mixin.
- Owned JNI arrays, bounded input, timeout, panic containment, session fallback.
- Bundled natives for four desktop targets, SHA-256 extraction validation.
- Differential tests using the actual Minecraft MEAN implementation.
- Documentation, contributor workflow and repository hygiene rules.
- Outstanding: physical-GPU measurements and end-to-end performance evidence.

## M1 — qualify texture preparation (in progress)

- Added an opt-in end-to-end vanilla/WebGPU mipmap benchmark with equality checks;
  machine-specific measurements and threshold selection are still pending.
- Run the complete resource-pack matrix and reload stress tests.
- Measure CPU preparation, JNI copies, dispatch/readback and total reload time.
- Choose the threshold from representative Intel/AMD/NVIDIA/Apple measurements.
- Evaluate batching and reuse before expanding the GPU stage.
- Add cutout/coverage handling only if it can remain exactly compatible and faster.
- Add verified additional native targets, dependency audit and release provenance.

## M2 — Blaze3D backend integration and first world pass (in progress)

### Renderer backend lifecycle

Minecraft 26.2 already routes rendering through Blaze3D. The vanilla Graphics API
setting selects a backend preference (OpenGL or experimental Vulkan); it does not
enable or disable Blaze3D. Fe2O3 must therefore attach to the active Blaze3D
device, not write `options.txt` or force a backend.

- **Implemented:** after `RenderSystem.initRenderer`, read the selected
  `DeviceInfo.backendName()` and provide an OpenGL/Vulkan preference to the
  separate wgpu compute stage. On a Minecraft restart after the Graphics API
  setting changes, the new initialized device is detected automatically.
- **Implemented:** if the preferred wgpu API cannot initialize, retry normal
  wgpu adapter selection before the existing vanilla fallback.
- **Implemented; CI verified:** lifecycle callbacks now run before
  RenderSystem.initRenderer replaces a device, after the replacement is
  installed, and when GameRenderer.close begins. Device generations allow
  resources to reject stale handles.
- **Implemented; CI verified:** the client smoke test allocates a
  temporary 4-byte `GpuBuffer` on the active `GpuDevice`, verifies cleanup on
  detach, and verifies that it is rebuilt for a later generation. This covers
  real handle ownership and simulated reinitialization using the same device
  object, not actual renderer-device replacement or a production resource.
- **Remaining:** attach production resources needed by the first world pass and
  verify device recreation on hardware. The current compute stage still owns an
  independent wgpu device; this lifecycle API does not share it with Blaze3D.
- Preserve vanilla startup fallback; backend initialization failures leave
  vanilla rendering usable and disable Fe2O3's replacement for that session.
- Keep resource creation and draw submission in Minecraft's required render
  phases and thread. Do not assume OpenGL-specific state or raw Vulkan handles.
- Test clean first launch, OpenGL, Vulkan when available, changed preference,
  failed Vulkan startup recovery, window resize, resource reload and shutdown.
- Verify pixels and frame synchronization before replacing one actual pass.
  Roll back to that vanilla pass on unsupported capabilities or failure.

### Community backend reference

- **No validated shared-wgpu path yet:** see [ADR-010](docs/adr/010-community-backend-adoption.md) for the wgpu-mc and Vulcanite assessments, exact interop gap, and revalidation gates.
- **Next:** a development-only, Vulkan-only handle-capture spike. Capture exact instance/device/queue-family/extensions/features at device creation and establish whether the pinned wgpu command buffer can join Minecraft's pending submission. Keep vanilla rendering active; halt if identity or wgpu command-buffer extraction cannot be proven. Details and acceptance criteria are in ADR-010.

Before coding a production world pass, close these integration proof gates:

- Identify an actively maintained 26.2-compatible community seam that can be used within the dependency and license constraints, or document why none qualifies.
- Prove WebGPU work uses the active Blaze3D device and can be inserted into Minecraft's own frame submission; independent wgpu device creation or queue submission does not pass.
- Verify same-frame target access, format/sample count, image-layout transitions, synchronization, resize/reload/device-loss cleanup, and fallback to the unchanged vanilla pass.
- Implement a minimal offscreen or pass-equivalent experiment and compare captured output against vanilla under Vulkan validation; only then select the first world pass.
- Keep the current separate wgpu queue path for non-rendering compute only until these gates are met. No M2 renderer-completion claim or test JAR before a replaced pass and its rollback/parity checks pass.

Assess the current wgpu-mc/Electrum rewrite as a community solution. Its stated
goal is first full Blaze3D backend compatibility, followed by terrain replacement.
Pin and adopt it only after stable 26.2 compatibility, API maturity, distribution
terms, Java/native ABI, build process and the no-extra-user-mod requirement pass
review. It is a reference, not currently a safe runtime dependency.

- Define ownership of frame graph, color/depth targets, synchronization and
  resource reload lifecycle before connecting a second GPU backend.
- Avoid a separate raw-window surface unless interoperability with Minecraft's
  frame is proven.
- Replace one actual vanilla world pass with a tested rollback path; a diagnostic
  triangle or extra overlay does not count as pass replacement.

## M3 — world renderer and vanilla parity (planned)

- Terrain extraction and meshes, full cubes, arbitrary baked partial-block models.
- Biome tint, AO, lightmaps, emissive effects, cutout, translucent sorting, fluids.
- Entities, block entities, items, skins, armor, glint, shadows, outlines.
- Particle classes, weather, sky/celestials, clouds, fog and all dimensions.
- Resource packs, animated textures, custom models, reload and resizing lifecycle.
- Publish image comparisons and performance captures; retain unsupported fallback.

## M4 — stable release and version ports (planned)

- Complete the parity matrix with hardware coverage and no known regressions.
- Demonstrate improvements in frame-time percentiles, memory and loading costs.
- Keep native core independent; create a separately tested adapter for each game
  version. Do not widen the Minecraft version range to imply untested support.
