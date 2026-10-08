# Community projects and notices

This project uses wgpu (MIT/Apache-2.0), jni-rs (MIT/Apache-2.0), pollster
(MIT/Apache-2.0), bytemuck (MIT/Apache-2.0/Zlib), Fabric Loader/Mixin, Fabric Loom,
Gradle and JUnit. Transitive dependencies retain their own licenses. Cargo.lock
is the native dependency inventory. Release packaging must preserve applicable
third-party license texts, not just this summary.

Primary references:

- WebGPU specification: https://www.w3.org/TR/webgpu/
- wgpu: https://github.com/gfx-rs/wgpu
- Compute API reference example (v26.0.1): https://github.com/gfx-rs/wgpu/tree/v26.0.1/examples/features/src/hello_workgroups
- jni-rs: https://github.com/jni-rs/jni-rs
- Fabric 26.2 migration: https://fabricmc.net/2026/06/15/262.html
- Fabric example project: https://github.com/FabricMC/fabric-example-mod
- Fabric documentation: https://docs.fabricmc.net/

The native pipeline follows the established wgpu buffer/dispatch/readback model.
Minecraft code and assets are not distributed by this repository. The mod calls
Minecraft's public API and uses its running color tables for compatibility.

