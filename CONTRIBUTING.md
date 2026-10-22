# Contributing

Read README.md, ROADMAP.md, docs/DEVELOPMENT_BRIEF.md and
docs/ARCHITECTURE.md before changing integration code. Fe2O3 is focused on
mipmap optimization and a Sodium addon path. Keep the change scoped, preserve
vanilla fallback for unsupported requests, and do not claim broad Sodium-addon
compatibility from a single setup.

Use Minecraft's own mipmap behavior as the output oracle. For graphics or
integration changes include a focused reference test, the exact build and test
evidence, affected compatibility-matrix entries, and controlled performance
results before claiming a speedup. Do not weaken tests to accept a mismatch.

Rust owns bounded native computation and JNI lifetime validation. Java owns
Minecraft, Fabric, and version-specific Sodium integration. Check callers
before changing the ABI. Every public ABI change needs matching tests and a
rationale in docs/DECISIONS.md. Do not copy Sodium implementation code without
reviewing its license and distribution obligations.

Run rustfmt, Clippy, native tests, Gradle differential tests, packaged-JAR
validation, and the relevant live-client test. Record limitations in
docs/VERIFICATION.md and follow docs/REPOSITORY_HYGIENE.md.

For bug reports include game/mod versions, OS/CPU/GPU/driver, selected backend,
Sodium/addon versions, resource packs, JVM flags, and a minimal reproduction.
Share only relevant logs after checking them for private information. For
suspected security issues avoid posting secrets or weaponized payloads in a
public issue.
