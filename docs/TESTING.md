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
- Platform identifier mapping. Mandatory adapter execution in Linux CI; native
  compilation alone on other platforms is not GPU validation.
- A separate Fabric client test uses the packaged mod and real Mixin, checks a
  known output pixel and execution counter, confirms CUTOUT stays on vanilla,
  creates a block/entity/particle/sky scene, and asserts another GPU chain is
  generated during resource reload.
  Screenshots are captured as smoke evidence, not asserted full parity baselines.
  CI requires the completion marker and both non-empty scene screenshots, so a
  successful client launch without the test mod cannot pass this gate.

Record the final commit, Actions run, report counts, native backend/device and
remaining failures in VERIFICATION.md. Passing tests must not be inferred from
a successful compile. Do not mark a manually described scenario as executed.

## Manual visual parity matrix — not yet executed

| Area | Required cases | Current draw path | Verification |
| --- | --- | --- | --- |
| Terrain/full blocks | All directions, biome tint, AO, light levels 0–15, caves, chunk boundaries | Vanilla | Pending |
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

## Performance protocol — not yet executed

Measure a disabled baseline against enabled runs of the **same JAR**. Capture
adapter/device, driver, CPU, memory, OS, Minecraft backend, packs and dimensions.
Separate cold startup, shader compilation, warm resource reload and steady-state
frame times. Alternate the two modes, collect at least 10 samples after warmup,
and report median/p95/p99 plus ranges. Measure total reload latency and peak
resident/native/GPU memory; dispatch time alone excludes JNI and readback costs.

No FPS or resource-reload speedup is currently claimed. Do not call a software
Vulkan result a hardware speedup. A loading-stage optimization should not be
presented as a world-renderer benchmark. Set the default threshold only after
end-to-end measurements show a benefit without visual regressions.

