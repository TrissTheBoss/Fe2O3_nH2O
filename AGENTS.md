# Contributor and coding-agent guidance

Read README.md, ROADMAP.md and docs/ARCHITECTURE.md before changing rendering.
Preserve vanilla behavior outside the explicitly supported native path. Never
claim an optimization, gameplay parity or platform support from compilation alone.

- Rust/WGSL owns graphics; Java owns the exact Minecraft-version adapter.
- Keep the mod client-only and require no additional user-installed native tools.
- Use Minecraft as the test oracle; do not replace failures with approximations.
- Keep Cargo.lock, version pins, notices and documented ABI in sync.
- Run Rust format/check/test, Gradle differential tests, packaged JAR validation
  and the relevant live-client test for integration changes.
- Follow docs/REPOSITORY_HYGIENE.md and preserve existing user work/history.
- Update docs/VERIFICATION.md with actual evidence and explicit untested areas.
- No generated binaries, game assets, logs, screenshots or secrets in git.

