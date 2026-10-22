# Repository hygiene plan

## Tracked source

Track Java, Rust, WGSL, tests, exact dependency configuration and Cargo.lock;
track documentation and CI configuration. Do not commit compiled JARs, native
binaries, target/, build/, .gradle/, run/, logs, crash dumps, credentials,
screenshots, Minecraft assets, downloaded game JARs or decompiled game sources.
The project purpose is texture mipmap optimization, with Sodium addon and Vulkan
integration as planned work; preserve this scope in public descriptions and docs.

## Change workflow

Use one focused feature/fix branch and a reviewable PR. Preserve user changes and
existing history. No force-push, unrelated reformatting, branch deletion or
automatic merge. Keep main usable; describe implemented scope and gaps in each PR.
Before review, run relevant tests, inspect the complete diff, verify resource
contents, confirm no generated output or secrets, and update status docs.

Protect main with an active GitHub ruleset: require pull requests, block
force-pushes and deletion, and require native (ubuntu-24.04), native
(windows-2022), native (macos-15), native (macos-15-intel) and verify checks
before merging. Do not require publish-prerelease on branch PRs; it only runs
for release tags.

## Dependencies and CI

Use published community dependencies with reviewed licenses. Pin toolchain and
direct dependency versions, commit lockfiles, and use locked Cargo builds after
bootstrap. Dependency upgrades are separate changes with CI evidence and a
decision-record update when behavior or architecture changes. Use read-only
Actions permissions, bounded job times and artifact retention. Pin action
revisions and review automated update PRs; do not execute fork code with write credentials.

Treat the currently tested standalone alpha separately from future Sodium addon
integration. Identify actual Sodium integration APIs and supported extension
points before implementation; test coexistence with Sodium and its extension
ecosystem instead of promising blanket compatibility. Keep Vulkan renderer
integration and Rust optimization of profiled Sodium hot paths as separate,
measured milestones.

## Documentation maintenance

README is the user entry point; ROADMAP is the milestone status; ARCHITECTURE is
the ownership/ABI reference; DECISIONS records rationale; TESTING owns the parity
matrix; VERIFICATION records executed evidence. Keep one source of truth per
subject and link rather than duplicate. Every feature PR updates affected docs.
Never change pending to verified without a run or capture reference.

## Release gate

Build from an identified commit; retain reports, checksums and dependency notices
with the artifact. Release notes must distinguish texture mipmap work from
renderer integration. Do not ship game assets or claim vendor endorsement. Do
not publish a stable release until documented correctness, backend, performance,
and compatibility gates pass. Tag names must match gradle.properties; never
move or reuse published tags. Automated alpha/beta/rc releases must pass the full
build workflow and attach the verified JAR with its SHA-256 sidecar. Keep the tag
protected from updates and deletions with an active v* ruleset. See RELEASES.md
for the maintainer procedure and stable-release gate.
