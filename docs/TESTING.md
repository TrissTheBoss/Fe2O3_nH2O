# Testing, parity and performance

## Automated coverage

- Rust: invalid and oversized shapes, excessive levels, odd/rectangular flooring.
- JVM: color lookup recovery compared with ARGB.meanLinear over 100,000 seeded inputs.
- GPU differential: complete chains against actual 26.2 MipmapGenerator for six
  dimensions and four patterns (opaque random, arbitrary alpha, checkerboard,
  constant color), including odd sizes, strips and a 512x512 nine-level chain.
- Source ownership/content, opaque AUTO GPU eligibility and pixel-exact output,
  specialized pack-strategy fallback, supplied mip fallback, zero/excessive
  levels, JNI invalid shapes and recovery after rejection.
- An opt-in informational benchmark compares complete vanilla and WebGPU/JNI
  mipmap calls on three opaque image sizes after warmup; it checks output equality
  and reports median/p95 without asserting a performance win.
- Platform identifier mapping. Mandatory adapter execution in Linux CI; native
  compilation alone on other platforms is not GPU validation.
- Blaze3D device epoch state tests cover identity-based idempotence, replacement,
  detach and failed-initialization state. The live client observes a nonzero
  generation from the RenderSystem.initRenderer hook, allocates a temporary
  4-byte `GpuBuffer` through the active `GpuDevice`, and verifies idempotent
  registration, same-device initialization idempotence, close on detach, and
  reallocation after a simulated lifecycle reattach. It also injects initialization and release callback failures and
  verifies that failed resources are removed without blocking healthy resources.
  It uses the active device object and does not exercise actual renderer-device
  replacement, hardware, or a production rendering resource.
- The opt-in Blaze3D block-outline Mixin is enabled in the production-client
  smoke run. The test supplies a deterministic hit result and asserts the
  `LevelRenderer` hook executes. Current CI does not populate the extracted
  `BlockOutlineRenderState`, so a non-empty custom submission, visual parity and
  performance improvement are not verified.
- A separate Fabric client test uses the packaged mod and real Mixin, checks a
  known output pixel and execution counter, confirms CUTOUT stays on vanilla,
  creates a block/entity/particle/sky scene, and asserts another GPU chain is
  generated during resource reload.
  Screenshots are captured as smoke evidence, not asserted full parity baselines.
  CI requires the completion marker and both non-empty scene screenshots, so a
  successful client launch without the test mod cannot pass this gate.

Record the final commit, Actions run, report counts, native backend/device and
remaining failures in VERIFICATION.md. Passing tests must not be inferred from
a successful compile. Record manually reported results with their evidence and
limitations; don't present a subjective observation as a measurement.

## Manual visual parity matrix — not yet executed

| Area | Required cases | Current draw path | Verification |
| --- | --- | --- | --- |
| Terrain/full blocks | All directions, biome tint, AO, light levels 0–15, caves, chunk boundaries | Vanilla | Pending |
| Block-selection outline | Voxel shape, line width, translucent target, high contrast, debug shapes | Blaze3D opt-in; vanilla default/debug fallback | Live hook smoke pending; image diff pending |
| Non-full blocks | Stairs, slabs, fences, panes, doors, plants, redstone, custom baked models | Vanilla | Pending |
| Entities/block entities | Skins, armor, items, glint, outlines, shadows, chests, signs, banners | Vanilla | Pending |
| Particles | Opaque, translucent, lit, weather and item/block particles | Vanilla | Pending |
| Sky/weather | Day/night, sunrise, stars, clouds, rain/snow, all dimensions | Vanilla | Pending |
| Fluids/transparency | Water/lava, glass overlap, sorting and underwater fog | Vanilla | Pending |
| Packs | Default, 16/64/256/512px, animated strips, alpha, custom models, supplied mips, all strategies | Mixed preparation, vanilla drawing | Synthetic pixel tests only |
| Lifecycle | Repeated F3+T, pack enable/disable/order, world changes, resize, fullscreen, mip level changes | Vanilla plus native stage | Pending |
| Backends/hardware | Minecraft OpenGL and Vulkan; Intel/AMD/NVIDIA, Apple Metal, Windows DX12 | Independent compute | Linux software Vulkan planned |
| Failure behavior | Unsupported arch, no adapter, device loss, noexec temp, shutdown during reload | Vanilla fallback where recoverable | Partial automated coverage |

Use the same world copy, seed, camera pose, time, weather, resolution, packs and
settings for vanilla and modified captures. Record versions and hashes. Compare
images with an exact diff where deterministic; mask only explained temporal
effects and retain both originals and diff. Never overwrite a baseline simply
because a test changed. Keep captures outside git in linked test artifacts.

## Performance protocol — controlled benchmark pending

The project owner reports two brief same-world/same-settings observations using
the FPS Display mod on Windows, Minecraft 26.2 at 854x480, Java 25.0.1, Fabric
Loader 0.19.5 and an AMD Radeon RX 6800 XT, with the Fe2O3 alpha.1 JAR:
OpenGL was reported as at least 3,300 FPS; Vulkan was reported as 2,100–2,200
FPS, with no reading above 2,300. The launcher logs show Minecraft initialized
on OpenGL in the first run and Vulkan in the second. In both, Fe2O3 initialized
its WebGPU mipmap adapter on Vulkan on the same Radeon GPU and generated a
16x512 two-level chain. In-game chat in the logs records the FPS statements and
the owner's same-world/same-settings note.

These are useful real-hardware backend smoke observations and show the game
started on both Minecraft backends with the mod present. They are not a
controlled performance benchmark: the logs do not preserve raw FPS samples,
sampling intervals, frame-time distributions, a no-mod baseline, screenshots,
or resource-pack/stress coverage. They do not show that mipmap generation
caused the measured steady-state FPS. Preserve the owner's report while
distinguishing it from a measured mod speedup.

For a controlled comparison, use the same world, camera, scene, view and
simulation distances, display resolution, resource packs and graphics settings.
Capture raw FPS/frame-time samples from the same measurement tool, alternate
backend runs after warmup, and record median/p95/p99 over a defined interval.
Compare Fe2O3 enabled against disabled on each backend. Also measure reload
latency and peak resident/native/GPU memory; dispatch time alone excludes JNI
and readback costs.

Do not call a software Vulkan result a hardware speedup. A loading-stage
optimization should not be presented as a world-renderer benchmark. Set the
default threshold only after end-to-end measurements show a benefit without
visual regressions.
