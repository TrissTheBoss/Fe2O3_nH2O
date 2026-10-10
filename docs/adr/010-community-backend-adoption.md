# ADR-010: Do not pin a community Blaze3D backend yet

- Status: accepted
- Date: 2026-10-10
- Scope: M2 backend integration candidate selection

## Context

M2 needs a stable way to use WebGPU-backed resources in Minecraft 26.2's
Blaze3D frame and device lifecycle. A candidate must be compatible with the
target Fabric/Minecraft version, have a usable and reviewable Java/native API,
preserve Minecraft's frame and resource ownership, have acceptable distribution
terms, build reproducibly, and add no separately installed user mod.

The wgpu-mc project is the closest community implementation found. Its
README describes Electrum as a Fabric integration using Project Panama. It
also says the project is undergoing a rewrite to achieve full Blaze3D backend
compatibility, with terrain replacement as later work. The repository has no
published releases at the time of review. The old wiki and historical source
describe older renderer hooks and are not evidence of compatibility with
Minecraft 26.2.

Sources reviewed on 2026-10-10:

- https://github.com/wgpu-mc/wgpu-mc
- https://github.com/wgpu-mc/wgpu-mc/releases
- https://github.com/wgpu-mc/wgpu-mc/wiki

## Decision

Do not add or pin wgpu-mc/Electrum as a Fe2O3 runtime or build dependency
for M2 at this time. Continue to use its public design and implementation as
a community reference. Keep Fe2O3's existing Java 26.2 adapter and Rust/wgpu
compute device independent; do not imply that lifecycle callbacks provide
interoperability or frame access.

This is a readiness decision, not a rejection of the project. Re-evaluate
against an immutable upstream commit when the following evidence is available.

## Adoption gates

All gates must pass before dependency adoption:

1. **Version/API:** documented build and successful launch on the exact supported
   Fabric and Minecraft 26.2 versions; stable backend interfaces or a pinned
   commit with a maintained compatibility adapter.
2. **Frame ownership:** a reviewed path to Minecraft's active color/depth targets,
   command submission and synchronization, resize, resource reload, device loss,
   and shutdown. No competing window surface unless interop is demonstrated.
3. **Parity and fallback:** an opt-in first-pass implementation with image-based
   vanilla comparisons and a reliable return to vanilla on unsupported or failed
   initialization.
4. **Distribution:** license and notices reviewed for all vendored or bundled
   components, including generated/native artifacts.
5. **Build/release:** reproducible CI build and pinned source/native ABI; no extra
   end-user mod or separately installed runtime tool.

Record the reviewed upstream commit, license evidence, build commands, test
results and unresolved risks in this ADR when re-evaluating. Until then, the
first world-pass implementation remains blocked on an integration path that
passes these gates; do not substitute a diagnostic overlay or a second surface.
