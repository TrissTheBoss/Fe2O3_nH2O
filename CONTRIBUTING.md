# Contributing

Read README, ROADMAP and docs/ARCHITECTURE.md first, then docs/BUILDING.md.
Keep changes small and preserve vanilla behavior on unsupported paths. For
graphics changes include a failing/reference test, implementation, exact build
and test evidence, affected parity rows and performance results if claiming a
speedup. Use upstream Minecraft behavior as the oracle, not a copied approximate
implementation. Don't weaken tests to accommodate a mismatch.

Rust owns graphics and JNI lifetime validation; Java owns version-specific
Minecraft integration. Check callers before changing the ABI. Every public ABI
change needs matching tests and docs/DECISIONS.md rationale. Run rustfmt, Clippy,
native tests and Gradle tests. Follow docs/REPOSITORY_HYGIENE.md.

For bug reports include game/mod versions, OS/CPU/GPU/driver, selected Minecraft
backend, enabled resource packs, JVM flags and a minimal reproduction. Share only
relevant logs after checking them for private information. For suspected security
issues avoid posting secrets or weaponized payloads in a public issue.

