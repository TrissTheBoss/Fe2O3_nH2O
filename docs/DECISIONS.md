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

## ADR-006 — Integrate through Blaze3D; evaluate the community backend first (2026-10-08, accepted)

M0 owns an independent wgpu device for texture computation and returns mip pixels
to Minecraft. That proves no framebuffer sharing, window presentation, or world
draw integration. Minecraft 26.2 has an optional Vulkan backend; Fabric's 26.2
rendering guidance requires the Blaze3D abstraction and describes separate render
data extraction and drawing phases. A parallel raw-window wgpu surface would
create a second presentation path without proving that it can safely share
Minecraft's frame graph, depth buffer, or synchronization.

The community `wgpu-mc` / Electrum project is the closest existing solution.
Its current README says it is rewriting for Minecraft's newer renderer and puts
full Blaze3D backend compatibility ahead of terrain replacement. The current
Fabric metadata targets Minecraft 26.2-rc.1 and requires Fabric API, so it is
not a drop-in runtime dependency for this Fabric Loader-only mod.

Decision: the vanilla Graphics API preference is not a Blaze3D toggle. Do not
rewrite `options.txt` or force OpenGL/Vulkan on first startup or after a setting
change. Let Minecraft initialize and recover its selected backend, then bind
any future renderer resources to the active Blaze3D device on startup and
recreate them when that device changes. On failed integration, leave Minecraft's
vanilla path intact. Assess wgpu-mc/Electrum before implementing an independent
backend; do not create a separate native surface or a demonstration overlay as
a substitute for a vanilla pass. Before adopting upstream code, pin an exact
revision and verify stable 26.2 compatibility, API maturity, Java/native ABI,
distribution terms, build process, and the no-extra-user-mod requirement. Keep
the existing M0 texture stage separate until a tested backend lifecycle can
own real frame output.

Sources:
- [Minecraft 26.2 release notes: Graphics API and startup fallback](https://feedback.minecraft.net/hc/en-us/articles/46690753273997-Minecraft-Java-Edition-26-2)
- [Fabric 26.2 rendering concepts](https://docs.fabricmc.net/develop/rendering/basic-concepts)
- [Fabric world rendering and extraction/drawing phases](https://docs.fabricmc.net/develop/rendering/world)
- [wgpu-mc / Electrum upstream status](https://github.com/wgpu-mc/wgpu-mc)
- [wgpu-mc / Electrum Fabric metadata](https://github.com/wgpu-mc/wgpu-mc/blob/main/fabric/src/main/resources/fabric.mod.json)
- [wgpu raw surface lifetime requirements](https://docs.rs/wgpu/26.0.1/wgpu/enum.SurfaceTargetUnsafe.html)

Consequences: M2 starts with device lifecycle and backend compatibility
validation, then proceeds to one actual world pass with a vanilla rollback.
No M2 rendering code or visual parity is claimed by this decision.

## ADR-007 — Follow the initialized Blaze3D backend as a wgpu preference (2026-10-08, accepted)

The owner asked Fe2O3 to adapt when Minecraft's Graphics API changes. Minecraft
26.2 initializes its Graphics API through Blaze3D, and its device information
exposes the selected backend. Detect the initialized device after
`RenderSystem.initRenderer`; pass a normalized OpenGL/Vulkan preference into the
existing independent wgpu compute stage. Do not edit Minecraft options or select
its backend. If matching wgpu initialization fails, retry wgpu's ordinary
automatic adapter selection and preserve the existing vanilla fallback on total
failure. A setting change takes effect after Minecraft restarts and initializes
the newly selected device.

This is backend preference alignment, not graphics-device interoperability:
Fe2O3 still owns an independent wgpu device, transfers mip pixels back to Java,
and performs no world draw. Binding to Blaze3D resources and replacing an actual
world pass remain separate milestones. Test both Minecraft presentation backends
on physical hardware before making compatibility or performance claims.

Evidence: the owner supplied successful 26.2 OpenGL and Vulkan launches with
the released mod, while their logs show the independent wgpu stage used Vulkan
for both. Official 26.2 migration notes document `DeviceInfo#backendName`;
Fabric's 26.2 rendering guide requires the Blaze3D abstraction. The implementation
must still pass 26.2 compilation and live-client CI before merge.


## ADR-008 — Tie future renderer resources to Blaze3D device generations (2026-10-08, accepted)

Context: backend preference matching only selects an API for the independent
wgpu compute device. A future Blaze3D render pass needs resources created for
the actual active Minecraft device and deterministic release before device
replacement or renderer shutdown.

Decision: observe RenderSystem.initRenderer before and after initialization,
release registered resources while the prior device is still current, and issue
ready callbacks with the new GpuDevice and a monotonically increasing
generation. Release remaining resources at GameRenderer.close. Keep all
callbacks on Minecraft's render thread. Isolate resource callback failures from
Minecraft startup and shutdown, and remove a resource that fails to initialize
or release. Do not expose backend-specific raw handles through this lifecycle.

Alternative: continue using the independent wgpu device or add a separate
window/surface. Those paths do not prove interoperability with Minecraft's
frame graph and synchronization. Do not add an overlay as a substitute.

Consequences: this supplies the device-owned resource lifecycle boundary but
does not create a render pass or share the existing mipmap compute device.
A real resource must register and pass backend, recreation, reload, resize and
shutdown tests before an actual vanilla pass can be replaced. The callback path
and mixin signatures require Minecraft 26.2 CI and live-client validation.


## ADR-009 — Gate world-pass work on a supported Blaze3D integration path (2026-10-09, accepted)

Context: PR #12 adds device lifecycle callbacks, but the project still needs to
replace one actual vanilla world pass to complete M2. The closest community
renderer, wgpu-mc/Electrum, documents a rewrite for Minecraft's newer rendering
architecture. Its stated sequence is full Blaze3D backend compatibility first,
then experimentation on terrain and chunk meshing. On 2026-10-09, its public
GitHub repository had no published releases and no open pull requests. Fabric's
26.2 world-rendering API documents extraction/drawing and custom rendering
events; these add custom content and do not themselves replace a vanilla pass.

Decision: do not pin the upstream default branch, vendor or adapt its unfinished
implementation, or treat an additive Fabric event as a replaced vanilla pass.
Continue tracking upstream and only adopt a specific immutable revision after
checking 26.2 compatibility, build and test process, licensing, native ABI,
Blaze3D device/frame ownership, and whether it can ship without another
user-installed mod. A first-pass change must use that supported path or a
separately reviewed stable public integration point, keep a vanilla rollback,
and prove frame output and lifecycle behavior.

Alternatives: add a diagnostic overlay now (does not satisfy pass replacement);
copy unfinished upstream code (creates an unmaintained fork and bypasses the
requested community solution); build a second renderer/surface independently
(increases interoperability and synchronization risk without proving access to
Minecraft's frame).

Consequences: M2 remains in progress. The existing lifecycle infrastructure is
useful groundwork, but no production rendering resource or world pass is
claimed. This is a readiness gate, not a claim that later renderer work is
impossible.

Evidence:
- [wgpu-mc/Electrum README](https://github.com/wgpu-mc/wgpu-mc#electrum--rust-based-rendering-engine-mod-for-minecraft)
- [wgpu-mc releases](https://github.com/wgpu-mc/wgpu-mc/releases)
- [Fabric 26.2 world rendering](https://docs.fabricmc.net/develop/rendering/world)
- [Fabric 26.2 rendering concepts](https://docs.fabricmc.net/develop/rendering/basic-concepts)
