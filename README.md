# Fe2O3_nH2O

Minecraft Rust rendering engine optimization project using the community-maintained
[`wgpu`](https://wgpu.rs/) implementation of [WebGPU](https://www.w3.org/TR/webgpu/).

**Status: experimental first rendering stage, not a complete renderer or a proven FPS optimization.**
Targets **Minecraft Java 26.2 / Fabric Loader 0.19.5+ / Java 25**.

## What actually runs

The mod replaces eligible **MEAN texture mipmap generation** with a Rust/WGSL
compute pipeline. All mip levels run on the GPU in one submission, followed by
one readback. It uses the running game's integer color tables, preserving its
color arithmetic. This is texture preparation during loading/reloading, not a
replacement for world draw calls. There is no controlled evidence that it raises
steady-state FPS.

The project owner reports flawless play and very high FPS in a Windows test on
an AMD Radeon RX 6800 XT. The attached log confirms Minecraft launched through
OpenGL, the WebGPU mipmap stage initialized on the same GPU through Vulkan, and
a mip chain was generated. No numeric FPS capture or disabled-mod comparison
was included, so this is a positive hardware smoke report, not a measured
speedup attributable to Fe2O3.

Terrain, full and partial blocks, entities, particles, skies, weather, fluids,
lighting, animations and GUI drawing still use Minecraft's renderer. Their full
visual parity has **not** been tested. Cutout strategies, supplied mip chains,
unsupported image formats/sizes and unavailable native backends use vanilla.
Resource-pack images are consumed after Minecraft loads them; the mod does not
replace the resource-pack loader, model baker or animation metadata handling.

## Blaze3D and Graphics API selection

Minecraft 26.2 already renders through Blaze3D. Blaze3D is the renderer layer
between Minecraft's rendering code and its selected graphics backend. The
vanilla **Graphics API** video preference chooses how Blaze3D is backed
(currently OpenGL or experimental Vulkan); Blaze3D itself is not a preference
that a mod can switch to.

Fe2O3 does not edit `options.txt`, force OpenGL/Vulkan, or run a competing
renderer. Its current mipmap compute stage uses an independent wgpu device and
returns pixels to Minecraft. World draws remain in Minecraft/Blaze3D. This keeps
the mod compatible with the backend Minecraft successfully initializes and lets
Minecraft apply its own startup crash fallback.

A future Blaze3D backend integration must bind to Minecraft's active device at
initialization, and refresh GPU resources when Minecraft recreates or changes
that device. It must not change the user's Graphics API preference. Automatic
binding/rebinding and a world-pass replacement are **planned, not implemented**;
see [the renderer roadmap](ROADMAP.md#renderer-backend-lifecycle).

## Install and use

1. Install Minecraft **26.2**, Java 25 and Fabric Loader **0.19.5 or later**.
2. Download the JAR and `.sha256` file from the
   [GitHub Releases page](https://github.com/TrissTheBoss/Fe2O3_nH2O/releases).
   Releases are experimental prereleases. Check the release notes and verify
   the JAR checksum before installing.
3. Put `fe2o3-nh2o-0.1.0-alpha.1.jar` in your instance's `mods` directory.
   Do not install the `-sources.jar`.

No other mod is required, including Fabric API. Rust libraries are bundled into
platform native binaries inside the JAR; users do not install Rust, wgpu, a
browser or a separate JNI mod. A working system graphics driver is required for
GPU acceleration. Unsupported platforms fall back to vanilla.

Native build targets: Linux x86_64 (Ubuntu 24.04 baseline), Windows x86_64,
macOS arm64 and x86_64. Building a platform is not proof of in-game compatibility.

Optional JVM arguments (restart required):

| Argument | Default | Purpose |
| --- | --- | --- |
| `-Dfe2o3.enabled=false` | enabled | Disable the replacement entirely |
| `-Dfe2o3.minPixels=4096` | 4096 | Minimum source image area, clamped to 4–4194304 |
| `-Dfe2o3.minPixels=4` | — | Exercise small vanilla textures during development |

Look for `WebGPU generated first Minecraft mip chain` in `logs/latest.log` to
confirm execution. An initialization or GPU failure disables this stage for the
session and logs the reason. Merely loading the mod does not prove GPU execution.
The default threshold is provisional; benchmark it before treating it as optimal.

## Build and contribute

Use JDK 25, Gradle 9.5.1, Rust 1.90.0 and Python 3:

```sh
cargo build --locked --manifest-path native/Cargo.toml --release
python tools/stage_native.py
gradle build
```

The local JAR contains the platform you built. GitHub Actions combines all four
native builds and requires software-Vulkan pixel comparisons against Minecraft
before publishing a JAR artifact. See [BUILDING](docs/BUILDING.md) for tests,
constraints and failure diagnosis.

## Project guide

- [Roadmap](ROADMAP.md): implemented stages and remaining milestones.
- [Development brief](docs/DEVELOPMENT_BRIEF.md): refined request and acceptance criteria.
- [Architecture and ABI](docs/ARCHITECTURE.md): ownership, integration and fallback.
- [Decisions](docs/DECISIONS.md): rationale, alternatives and consequences.
- [Testing and parity](docs/TESTING.md): automated coverage and unexecuted visual matrix.
- [Verification record](docs/VERIFICATION.md): exact commits, runs and remaining gaps.
- [Repository hygiene](docs/REPOSITORY_HYGIENE.md): branch, artifact and dependency rules.
- [Release policy](docs/RELEASES.md): version tags, alpha publication and release protections.
- [Contributing](CONTRIBUTING.md): workflow and documentation requirements.
- [Third-party notices](NOTICE.md): upstream projects and dependency policy.

Not an official Minecraft product. Not approved by or associated with Mojang or Microsoft.
