# Fe2O3_nH2O

Fe2O3 is an experimental client-side Minecraft performance project focused on
**texture mipmap generation**. Its new direction is to optimize this path as a
Sodium addon, investigate Vulkan support through the active rendering stack,
and move only measured, suitable hot paths into Rust.

**Current status:** the mod uses Rust, wgpu and WGSL to generate eligible
Minecraft mip chains on the GPU. It now registers an optional settings page in
Sodium's Video Settings through Sodium's public Config API. This is a GUI/config
integration; the mod does not replace Sodium rendering code, share Sodium GPU
resources, or optimize world-rendering frame time. The production mipmap hook
remains a Minecraft mixin, and Vulkan rendering integration and Rust replacements
for Sodium hot paths remain unimplemented.

## Project direction

The project will keep a narrow scope around texture mipmaps and closely related
rendering preparation. It will:

- Preserve Minecraft's output exactly for supported mipmap strategies and use
  vanilla for unsupported inputs.
- Integrate as an addon through documented, version-pinned Sodium extension
  points wherever the target Sodium release provides them.
- Investigate Vulkan using the active Minecraft/Sodium graphics backend and its
  supported lifecycle. It will not force a backend or create an unrelated
  presentation path.
- Consider Rust implementations for profiled hot paths only when a maintained
  integration point can safely use them and controlled benchmarks show a gain.
- Test specific combinations of Minecraft, Fabric, Sodium, Vulkan support,
  resource packs, and other rendering mods before claiming compatibility.

“Compatible with every Sodium addon” is not a testable guarantee. The goal is
to use stable extension points, avoid unnecessary internal hooks, and publish
a compatibility matrix for combinations that have actually been tested.
See [the roadmap](ROADMAP.md) for phases and acceptance gates.

## What the current alpha does

A client mixin intercepts eligible MEAN texture mipmap generation and asks a
Rust/wgpu compute pipeline to generate the remaining levels. It uses
Minecraft's own integer color tables and falls back to vanilla for unsupported
strategies and inputs. This is texture preparation during resource loading or
reloading; it does not render terrain or raise steady-state world FPS by
replacing Sodium's renderer.

The project owner has reported successful Minecraft 26.2 launches on an AMD
Radeon RX 6800 XT using both OpenGL and Vulkan, with the mipmap stage generating
a test chain. These are backend smoke checks, not controlled performance
measurements or Sodium compatibility results. See
[the verification record](docs/VERIFICATION.md).

The current wgpu compute stage owns an independent device and returns mip
pixels to Minecraft. Matching the Minecraft backend preference is not the same
as sharing Sodium's device, frame graph, or Vulkan resources. Those integration
questions remain open.

## Install the current alpha

The current alpha targets Minecraft **26.2**, Fabric Loader **0.19.5+**, and
Java **25**. Sodium **0.9.2 for Minecraft 26.2** is optional and is required to
show the integrated settings page. Without Sodium, the standalone mipmap
accelerator still runs with its saved configuration.

1. Download the JAR and checksum from
   [GitHub Releases](https://github.com/TrissTheBoss/Fe2O3_nH2O/releases).
2. Put fe2o3-nh2o-0.1.0-alpha.2.jar in the instance's mods directory.
3. Check the release notes and verify the checksum before installing.

The JAR bundles native libraries for Linux x86_64, Windows x86_64, macOS arm64,
and macOS x86_64. Users do not install Rust or wgpu separately. A working
graphics driver is required for GPU execution; unsupported requests fall back
to vanilla where recovery is possible.

## Build and test

The current alpha uses JDK 25, Gradle 9.5.1, Rust 1.90.0 and Python 3:

    cargo build --locked --manifest-path native/Cargo.toml --release
    python tools/stage_native.py
    gradle build

Use the project roadmap and architecture docs for the new Sodium-addon work.
New dependencies, backend integrations, and claimed performance improvements
must be version-pinned and verified through differential tests, supported
hardware checks, and controlled benchmarks.

## Project guide

- [Roadmap](ROADMAP.md): current status, target phases, and merge gates.
- [Development brief](docs/DEVELOPMENT_BRIEF.md): product scope and acceptance criteria.
- [Architecture](docs/ARCHITECTURE.md): implemented mipmap path and planned addon boundaries.
- [Decisions](docs/DECISIONS.md): rationale and superseded project direction.
- [Testing](docs/TESTING.md): exact-output, addon, backend, and benchmark coverage.
- [Verification record](docs/VERIFICATION.md): completed runs and known limits.
- [Building](docs/BUILDING.md): toolchain and build instructions.
- [Contributing](CONTRIBUTING.md): development and evidence requirements.
- [Repository hygiene](docs/REPOSITORY_HYGIENE.md): branch and artifact rules.
- [Release policy](docs/RELEASES.md): prerelease and release gates.
- [Third-party notices](NOTICE.md): upstream projects and dependency notices.

Not an official Minecraft product. Not approved by or associated with Mojang or Microsoft.
