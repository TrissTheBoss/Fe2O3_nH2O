# Contributor and coding-agent guidance

Read README.md, ROADMAP.md and docs/ARCHITECTURE.md before changing rendering.
The project currently optimizes Minecraft texture mipmaps. Sodium addon
integration, Vulkan rendering integration, and Rust rewrites of profiled Sodium
hot paths are roadmap items, not implemented capabilities. Preserve vanilla
behavior outside an explicitly supported and tested path. Never claim an
optimization, gameplay parity or platform support from compilation alone.

- Java owns the exact Minecraft-version adapter; Rust/WGSL owns the current
  experimental mipmap compute path.
- Keep the mod client-only and require no additional user-installed native tools.
- Use Minecraft output as the mipmap correctness oracle; do not replace failures
  with approximations.
- Keep Cargo.lock, version pins, notices and documented ABI in sync.
- For Sodium integration, identify supported APIs and extension points first;
  test coexistence with Sodium and named addons before making compatibility claims.
- Keep Vulkan integration distinct from WebGPU backend selection until the
  rendering path has been implemented and verified.
- Rewrite Sodium hot paths in Rust only after profiling identifies a bottleneck
  and measurements show a benefit without changing behavior.
- Run Rust format/check/test, Gradle differential tests, packaged JAR validation
  and the relevant live-client test for integration changes.
- Follow docs/REPOSITORY_HYGIENE.md and preserve existing user work/history.
- Record executed evidence and untested areas in docs/VERIFICATION.md; do not
  mark planned or unverified work as complete.
- No generated binaries, game assets, logs, screenshots or secrets in git.
