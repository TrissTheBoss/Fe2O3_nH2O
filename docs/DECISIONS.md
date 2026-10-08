# Decision records

Keep records append-only when superseded: state which later record replaces them.
Each change must record context, decision, alternatives, consequences and evidence.

## ADR-001 — Native wgpu plus a small JVM adapter (2026-10-08, accepted)

WebGPU is a specification, not a Minecraft library. Use gfx-rs/wgpu and WGSL in
Rust, with Fabric Loader and its bundled Mixin for integration. Rust produces a
native shared library, not Java bytecode; the installable JAR packages those
binaries and Java classes. Use the established jni-rs crate for JNI. A browser
embedding or custom graphics API would add unnecessary runtime requirements.
“No other dependencies” means no extra user-installed mods, not zero Cargo/build
dependencies. Preserve upstream licensing and attribution.

## ADR-002 — Begin with standard mipmap preparation (2026-10-08, accepted)

Replacing all world rendering at once would make correctness and failures hard
to isolate. Replace the MEAN mipmap stage first, not an unrelated test overlay.
Minecraft 26.2 has several specialized alpha-coverage strategies and can modify
base images before generating cutout mips; leave those paths entirely vanilla.
The exact 26.2 bytecode was inspected in GitHub Actions before implementation.
Its MEAN path uses integer color tables, allowing exact GPU arithmetic. Tables
are recovered through the running game's public ARGB conversions, not copied
Minecraft implementation code or a floating-point approximation.

Consequences: the first milestone affects texture loading/reloading, not frame
draw performance. It adds transfer costs and may be slower. Benchmarks are a
gate for optimization claims; the default minimum area is provisional.

## ADR-003 — Fail back to vanilla and commit outputs atomically (2026-10-08, accepted)

Unsupported platforms, adapters and image requests should not prevent play.
Cancel the vanilla method only after a complete valid chain exists. Preserve
base images and close unpublished outputs. Native initialization or GPU errors
disable the stage for the session rather than repeatedly failing every sprite.
A failed native driver can still terminate the process; no universal recovery
claim is made. A broken mandatory mixin is a hard compatibility failure, not a
silently successful no-op.

## ADR-004 — Pin the game and toolchain (2026-10-08, accepted)

Use Minecraft 26.2, Loader 0.19.5, Loom 1.17.21, Gradle 9.5.1 and JDK 25.
These were resolved from Fabric release metadata. Use Rust 1.90.0 and wgpu
26.0.1, whose API was checked against its tagged upstream compute example.
The wgpu version is an explicit initial baseline, not a claim to be latest.
Upgrade only with lockfile review and all differential tests. Game version and
wgpu version numbers are unrelated. Build-only JUnit dependencies are not mods.

## ADR-005 — Exact oracle and honest evidence (2026-10-08, accepted)

Use Minecraft's unmodified MipmapGenerator as the differential oracle in JVM
tests where Mixin is not active. Assert the native path actually ran so fallback
cannot create a false positive. Require a Vulkan software adapter in Linux CI;
do not silently skip GPU tests there. Separate this evidence from native hardware
testing, a Mixin-enabled game launch, world rendering parity and performance.

