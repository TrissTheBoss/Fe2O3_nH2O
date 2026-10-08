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

## M2 — Blaze3D backend integration and first world pass (planned)

### Renderer backend lifecycle

Minecraft 26.2 already routes rendering through Blaze3D. The vanilla Graphics API
setting selects a backend preference (OpenGL or experimental Vulkan); it does not
enable or disable Blaze3D. Fe2O3 must therefore attach to the active Blaze3D
device, not write `options.txt` or force a backend.

- At client startup, query the initialized Blaze3D backend/device and create
  renderer resources against that device.
- Observe device/backend recreation after Graphics API changes or recovery, then
  discard stale GPU resources and rebuild against the new active device.
- Preserve vanilla startup fallback; backend initialization failures leave
  vanilla rendering usable and disable Fe2O3's replacement for that session.
- Keep resource creation and draw submission in Minecraft's required render
  phases and thread. Do not assume OpenGL-specific state or raw Vulkan handles.
- Test clean first launch, OpenGL, Vulkan when available, changed preference,
  failed Vulkan startup recovery, window resize, resource reload and shutdown.
- Verify pixels and frame synchronization before replacing one actual pass.
  Roll back to that vanilla pass on unsupported capabilities or failure.

### Community backend reference

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
