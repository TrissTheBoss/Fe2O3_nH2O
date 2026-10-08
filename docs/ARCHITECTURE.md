# Architecture and ABI

## Data flow

Minecraft's resource loader and sprite pipeline call `MipmapGenerator.generateMipLevels`.
The client-only mixin tries `Mipmaps.tryGenerate` before vanilla executes. The
adapter accepts only a single RGBA source image, a valid 1–11-level chain, source
dimensions no larger than 2048, the configured minimum area, and MEAN (or AUTO
resolved to MEAN using Minecraft's Transparency). Every other request continues
unchanged through vanilla, including its alpha-coverage and source preprocessing.

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

## ABI v1

| Java native call | Input/output contract |
| --- | --- |
| initialize(int[1280]) | 256 linear values in 0..1023 then 1024 sRGB values in 0..255 |
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
Independent WebGPU buffers avoid sharing undocumented OpenGL/Vulkan handles.
The current stage does not depend on Minecraft's selected presentation backend.
This architecture has readback/copy costs and is not the future world renderer.

