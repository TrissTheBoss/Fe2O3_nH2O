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
  registration, close on detach, and reallocation after a simulated lifecycle
  reattach. It uses the active device object and does not exercise actual
  renderer-device replacement, hardware, or a production rendering resource.
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

## Focused mipmap and addon matrix

The release scope is mipmap preparation and the Sodium addon integration. Full
terrain, entity, sky, weather, particle, and general world-renderer parity are
not project acceptance gates.

| Area | Required cases | Status |
| --- | --- | --- |
| Mipmap correctness | MEAN/AUTO, odd dimensions, strips, alpha patterns, multiple levels | Exact software-GPU differential cases exist; expand realistic resource-pack cases |
| Vanilla fallback | Specialized strategies, supplied levels, unsupported sizes/formats | Existing strategy/fallback coverage; expand under the pinned game version |
| Resource lifecycle | Repeated reloads, world changes, resize, shutdown, native failure | Partial client smoke coverage; stress testing pending |
| Sodium integration | Pinned Sodium version, addon entry point, config API, fallback behavior | Not implemented |
| Sodium ecosystem | Iris and explicitly selected addon combinations, version by version | Not tested; no universal compatibility claim |
| Backend | Minecraft/Sodium OpenGL and Vulkan paths, active device ownership | Owner reported standalone OpenGL and Vulkan smoke runs; Sodium paths not tested |
| Rust hot paths | Named profile target, reference behavior, end-to-end measurement | No Sodium hot path selected or rewritten |

For each new Sodium integration test, record Minecraft, Fabric Loader, Sodium,
Java, GPU/driver, backend, addon set, resource packs, and exact mod commit.
Test a clean Sodium installation first, then add one addon at a time. A passing
Sodium startup alone does not establish that another addon works or that the
mipmap hook ran.

Keep differential baselines from Minecraft's own implementation. Assert that
the accelerated path executed so a fallback cannot create a false positive.
Capture artifacts outside git and retain originals, hashes, and diffs.

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
Compare the mipmap stage enabled and disabled on each backend. Measure end-to-end
resource reload latency, per-image CPU time, JNI copies, dispatch/readback, and
peak resident/native/GPU memory; dispatch time alone excludes integration costs.
Measure frame-time only if a later, profiled hot path affects frame rendering.

Do not call a software Vulkan result a hardware speedup. A loading-stage
optimization is not an FPS optimization. Set the default threshold only after
end-to-end measurements show a benefit without visual regressions.

