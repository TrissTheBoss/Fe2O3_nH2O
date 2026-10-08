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

## M2 — first world draw replacement (planned)

- Evaluate community wgpu/native-window integration against Blaze3D 26.2.
- Choose an explicit framebuffer ownership and presentation architecture.
- Prove depth, blending, synchronization and resource reload correctness.
- Replace one isolated pass with rollback to vanilla; do not label a diagnostic
  triangle or extra overlay a replacement of vanilla rendering.

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

