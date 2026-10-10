# Architecture and ABI

## Data flow

Minecraft's resource loader and sprite pipeline call `MipmapGenerator.generateMipLevels`.
The client-only mixin tries `Mipmaps.tryGenerate` before vanilla executes. The
adapter accepts only a single RGBA source image, a valid 1–11-level chain, source
dimensions no larger than 2048, the configured minimum area, and MEAN (or AUTO
resolved to MEAN using Minecraft's Transparency). Every other request continues
unchanged through vanilla, including its alpha-coverage and source preprocessing.

After `RenderSystem.initRenderer`, a small version-specific mixin reads
Blaze3D's active `DeviceInfo.backendName()`. The Java adapter maps OpenGL or
Vulkan to a wgpu backend preference; unknown values stay on automatic selection.
Rust tries that preferred API for its independent compute device and retries
automatic wgpu selection if the preferred API fails. Minecraft's backend is
never forced or changed.

Blaze3DDeviceLifecycle brackets RenderSystem.initRenderer: resource loss callbacks
run at the method head while the old device is current; ready callbacks run after
the new device is installed. A monotonically increasing generation identifies
each installed device. GameRenderer.close releases registered resources before
renderer shutdown. Resource callbacks must run on the render thread, must release
handles belonging to the provided device, and should treat generations as invalid
after the loss callback. Callback failures are logged and remove that resource
without aborting Minecraft's renderer lifecycle. The live client smoke test registers a temporary 4-byte Blaze3D buffer,
verifies cleanup during detach, and verifies that the same resource rebuilds its
buffer for a later generation. The test simulates detach/reattach on the active
device; it does not replace the renderer device. No production GPU resources
are registered yet; this remains lifecycle infrastructure. The block-selection outline experiment is submitted from Minecraft's extracted
`BlockOutlineRenderState` through the public Blaze3D `SubmitNodeCollector`
during the normal draw phase. The opt-in path duplicates the 26.2 outline
submission choices (shape, translucency, line width, and high-contrast layer)
and leaves the vanilla method in place as its default and debug-shape fallback.
It owns no persistent GPU handle, so it does not register a device resource.
The live client smoke test enables it and verifies a selected block outline
submission; visual equality with a vanilla baseline is still pending.

The existing wgpu mipmap device is independent and is not recreated or shared
through this API. In the chosen architecture, Blaze3D owns all Minecraft draw
submissions and render targets; Rust/wgpu remains for compute-only work. No raw
OpenGL/Vulkan handles or wgpu command buffers cross that boundary.

The Java adapter gets owned packed ARGB pixels and calls Rust through JNI.
Rust uploads one source buffer, dispatches each level with 8x8 workgroups, copies
all output levels into a single readback buffer, submits once and maps once.
The WGSL shader uses only integer arithmetic and the game's color lookup tables.
Java wraps the returned levels in NativeImages. Minecraft owns the resulting
chain and later performs its normal atlas uploads and drawing.

## Ownership and threading

- No image address, Java object reference or borrowed JNI array survives a call.
- Minecraft retains level zero; it is neither changed nor closed by the mod.
- Failed attempts close only newly allocated output images before fallback.
- Java synchronizes the adapter to bound concurrent staging memory during parallel
  resource loading. Rust also serializes device use and shutdown with a mutex.
- Each request is limited to 2048x2048 and 11 levels. GPU buffers are per-request;
  the device, pipeline and lookup buffer persist for the process lifetime.
- A shutdown hook drops native state. Resource reload has no stale sprite cache.
- Readback polling has a 10-second deadline. Adapter creation/driver calls still
  depend on OS behavior; this is not a hard process-wide watchdog.
- Native panics are caught at JNI boundaries; errors become Java exceptions and
  disable acceleration for that session. Driver crashes cannot be recovered by
  language-level exception handling.

## ABI v2

| Java native call | Input/output contract |
| --- | --- |
| initialize(int[1280], String) | 256 linear values in 0..1023 then 1024 sRGB values in 0..255; backend preference is `gl`, `vulkan` or `auto` |
| generate(int[], width, height, levels) | Exact source length; returns concatenated levels 1..N in row-major packed ARGB |
| shutdown() | Serialized release; safe when no renderer is initialized |

Every size is checked on both sides. Negative JNI integers are rejected by the
native bounds checks. Array length and color tables are validated before GPU use.
Native extraction uses an unpredictable temporary directory and verifies the
bundled SHA-256 digest. It loads no user-provided library path and downloads no code.

## Portability boundary

`native/` knows no Minecraft classes, mappings, models or world state. The Java
adapter and mixin are strictly pinned to Minecraft 26.2. A future backport must
provide that version's blend tables/semantics, integration and differential tests.
The selected Minecraft backend supplies a preference only: independent wgpu
buffers still do not share OpenGL/Vulkan handles, a surface, or synchronization
with Blaze3D. This is intentional: Blaze3D performs rendering while wgpu is used
for independent compute workloads such as mipmap generation. If matching wgpu
initialization fails, automatic adapter selection preserves the current compute
fallback behavior. The mipmap path still has readback/copy costs and makes no
world-rendering performance claim.

