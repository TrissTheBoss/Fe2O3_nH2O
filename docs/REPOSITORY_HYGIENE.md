# Repository hygiene plan

## Tracked source

Track Java, Rust, WGSL, tests, exact dependency configuration and Cargo.lock;
track documentation and CI configuration. Do not commit compiled JARs, native
binaries, target/, build/, .gradle/, run/, logs, crash dumps, credentials,
screenshots, Minecraft assets, downloaded game JARs or decompiled game sources.
The supplied README's purpose and project name are preserved.

## Change workflow

Use one focused feature/fix branch and a reviewable PR. Preserve user changes and
existing history. No force-push, unrelated reformatting, branch deletion or
automatic merge. Keep main usable; describe the implemented scope and gaps in
each PR. Before review: run relevant tests, inspect the complete diff, verify
resource contents, confirm no generated output or secrets, and update status docs.

## Dependencies and CI

Use published community dependencies with reviewed licenses. Pin toolchain and
direct dependency versions, commit lockfiles, and use locked Cargo builds after
bootstrap. Dependency upgrades are separate changes with CI evidence and a
decision-record update when behavior/architecture changes. Use read-only Actions
permissions, bounded job times and artifact retention. Pin action revisions and
review automated update PRs; do not execute fork code with write credentials.

## Documentation maintenance

README is the user entry point; ROADMAP is the milestone status; ARCHITECTURE is
the ownership/ABI reference; DECISIONS records rationale; TESTING owns the parity
matrix; VERIFICATION records executed evidence. Keep one source of truth per
subject and link rather than duplicate. Every feature PR updates affected docs.
Never change “pending” to “verified” without a run/capture reference.

## Release gate

Build from an identified commit; retain reports, checksums and dependency notices
with the artifact. Release notes must distinguish experimental texture work from
world renderer work. Do not ship game assets or claim vendor endorsement. Do not
publish a stable release until the documented parity and performance gates pass.

