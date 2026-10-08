# Refined development brief

Build Fe2O3_nH2O in TrissTheBoss/Fe2O3_nH2O as a client-side Fabric 26.2
rendering optimization project. Use Rust for the graphics implementation and
community-maintained wgpu for the WebGPU API. Package native libraries and the
minimal Fabric/JNI Java adapter in an installable JAR. Require no separately
installed mods except Fabric API when necessary; bundled implementation and
build dependencies are allowed.

Start with an actual replacement of a Minecraft rendering stage. Preserve
vanilla for paths that are not implemented or verified. Full vanilla appearance
and resource-pack support are the eventual release contract, covering terrain,
full/partial blocks, entities, block entities, particles, sky, weather, lighting,
fluids, GUI and lifecycle behavior. Distinguish renderer preparation from world
draw replacement, and compilation from gameplay verification.

Use GitHub Actions to compile native code, assemble the JAR and run meaningful
tests. Record exact versions, evidence, limitations and failure diagnosis. Keep
the repository free of build outputs and game assets. Maintain README, roadmap,
architecture, decision records, contribution rules and an explicit hygiene plan.
Plan future ports around version-specific adapters, without claiming support
before testing. Reuse established community libraries rather than inventing a
graphics API, native binding system or mod loader.

## Initial milestone acceptance criteria

1. Verified 26.2 interfaces; a real GPU mipmap stage used through a Fabric mixin.
2. A compiled JAR containing usable platform binaries; no user Rust installation.
3. Exact pixel comparisons against Minecraft's own implementation in CI.
4. Specialized resource-pack paths retain vanilla behavior through fallback.
5. Honest coverage matrix and reproducible build/test instructions.
6. A separate, explicit gate for actual game launch and gameplay parity.

The original full-renderer scope remains in the roadmap. This milestone is not
a promise that all of it can be implemented or verified in one development session.

