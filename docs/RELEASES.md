# Release policy

## Version and tag contract

The version in `gradle.properties` is the sole release version. Tags use the
`v` prefix and must match it exactly; the initial alpha is `v0.1.0-alpha.1`.
Create release tags only on commits already merged to `main`. The release job
checks ancestry and version equality before publishing. Do not move or reuse a
release tag. If an alpha needs correction, increment its prerelease number.

## Automated prereleases

Pushing a matching `v*` tag runs the same complete build and verification
workflow as normal CI. Publishing waits for all four native builds, mandatory
GPU differential tests, JAR/native/license validation and the live client smoke
test to pass. The workflow builds from the tag commit and publishes that exact
run's `fe2o3-nh2o-<version>.jar` plus its `.jar.sha256` sidecar. It does not
reuse an older temporary Actions artifact.

The workflow withholds distributable JAR artifacts until `ROADMAP.md` marks M2
implemented and CI-verified.

Only SemVer prereleases with `alpha.N`, `beta.N` or `rc.N` suffixes are accepted
by automation, and they are marked as GitHub prereleases. Stable tags fail the
release job intentionally. A stable release requires completing the visual
parity matrix and physical-hardware/end-to-end performance and memory gates in
`docs/TESTING.md`, updating `docs/VERIFICATION.md`, and changing this policy in
a reviewed PR. Do not describe software-Vulkan CI as hardware validation or
claim an FPS improvement without measurements.

The release job has `contents: write` permission only for the publish job. The
build and verification jobs remain read-only. No compiled files are committed
to Git. The release notes identify the source commit and Actions run and state
the limits of the experimental renderer.

## Protect release tags

Configure an active GitHub tag ruleset targeting `v*` at
[repository Rulesets](https://github.com/TrissTheBoss/Fe2O3_nH2O/settings/rules).
Enable **Restrict creations**, **Restrict updates** and **Restrict deletions**.
Grant bypass only to the maintainers who are authorized to cut a release; the
workflow token does not create tags and needs no bypass. Verify the ruleset is
active before making the first release tag. Rulesets apply to tags as well as branches; an admin or
role with repository-rules permission is required to manage them.

The initial alpha source was verified in
[Actions run 37805765573](https://github.com/TrissTheBoss/Fe2O3_nH2O/actions/runs/37805765573).
The release workflow performs a fresh verification for the release tag, so this
expired/temporary Actions artifact is evidence only and is not substituted as
the release asset.
