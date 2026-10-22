# Development brief

## Goal

Develop Fe2O3_nH2O as a client-side Minecraft **texture mipmap optimization**
project, with the goal of becoming an addon for a pinned Sodium release. Keep
the current Rust/wgpu implementation as a prototype and correctness baseline.
Investigate Vulkan rendering integration through the supported Minecraft and
Sodium renderer interfaces. Explore Rust implementations for additional
hot paths only after profiling establishes a suitable target and an end-to-end
benefit.

## Current implementation

The current alpha is a standalone Fabric 26.2 mod. It replaces eligible MEAN
mipmap generation with a Rust/wgpu compute path, compares results with
Minecraft's implementation, and falls back to vanilla outside its supported
input set. It does not yet integrate with Sodium, share Sodium renderer
resources, implement a Sodium extension, or replace Sodium hot paths.

## Product constraints

- Keep the scope on mipmap generation and directly related optimization work;
  do not expand into a full world renderer or map/minimap feature.
- Preserve exact Minecraft results for supported requests and use vanilla for
  unsupported cases.
- Use documented Sodium extension points where the pinned release provides
  them. Isolate unavoidable version-specific hooks and test their failure path.
- Treat Vulkan as a renderer integration problem. Selecting a wgpu Vulkan
  adapter does not prove the Minecraft/Sodium renderer is using or sharing it.
- Keep Rust free of Minecraft/Sodium classes; keep Java responsible for
  version-specific mod-loader, Sodium, and Minecraft integration.
- Prefer stable interoperability, but scope compatibility claims to explicit
  tested combinations. Do not promise support for every Sodium addon.
- Do not copy, bundle, or modify Sodium implementation code without reviewing
  its license and distribution obligations.
- Require controlled end-to-end measurements before claiming performance gains.
- Keep the mod client-only and package native dependencies so players do not
  need to install a Rust toolchain.

## Acceptance gates

1. Define and pin a target Minecraft, Fabric, and Sodium version.
2. Demonstrate the Sodium integration in a production client, with a fallback
   for unsupported requests and configurations.
3. Preserve differential mipmap output across supported image cases.
4. Establish Vulkan renderer ownership, synchronization, and lifecycle behavior
   before claiming Vulkan rendering support.
5. Select any additional Rust hot path from profiling, then verify semantics and
   end-to-end improvement against a Java/Sodium baseline.
6. Publish build, hardware, addon, and resource-pack coverage with exact
   limitations; do not equate successful compilation with compatibility.
