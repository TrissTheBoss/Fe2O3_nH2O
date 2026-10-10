# ADR-010: No validated shared-WebGPU Blaze3D path for M2 yet

- Status: accepted
- Date: 2026-10-10
- Scope: M2 device, target, and submission integration

## Context

M2 requires WebGPU work to consume Minecraft 26.2 frame resources on the same
device and in the same ordered frame submission, while preserving vanilla as a
fallback. Fe2O3 pins wgpu 26.0.1; the lockfile resolves wgpu-hal 26.0.6.

Candidate review found two relevant community projects:

- **wgpu-mc/Electrum:** its README says the project is undergoing a rewrite for
  current Blaze3D backend compatibility; terrain replacement is later work.
  Its old wiki/source do not establish Minecraft 26.2 compatibility.
- **Vulcanite (formerly MGF):** its README declares a 1.0.0 provider API for
  Minecraft 26.2. Its implementation exposes the live Vulkan instance, device,
  graphics queue and queue-family index, plus callback-scoped main-target image
  handles. Its documented provider seam runs at final composition after GUI,
  not at a world-only pass. Its 26.2 docs say only final color is verified; they
  do not promise depth, motion vectors or a separate scene-color target.

Vulcanite's compute contract requires recording work into Minecraft's live
graphics command encoder, then letting Minecraft submit the frame. It forbids a
provider from independently submitting or taking queue ownership. Fe2O3's
current wgpu path creates its own wgpu device and calls its own wgpu queue
submission. A second Vulkan queue submission cannot be assumed to observe
unsubmitted vanilla frame work, and a separately created wgpu device does not
share Minecraft's image handles or synchronization automatically.

wgpu 26.0.1 exposes unsafe HAL device construction through an Adapter, with the
safety requirement that the HAL device came from that same wgpu Adapter. The
current Fe2O3 initialization path requests its own adapter/device; merely
receiving Vulkan handles is not proof that a compatible wgpu HAL adapter,
device, command-buffer wrapping, queue ordering and lifetime path exists.
Vulcanite's VkInterop implementation may report a zero physical-device handle
when its device-creation negotiation snapshot is unavailable. No end-to-end
wgpu-on-Vulcanite provider build or runtime synchronization test has been run.

Vulcanite's README calls the API stable and identifies an MIT license for its
API artifact. The 26.2 implementation/player mod is under PolyForm Shield 1.0.0,
whose noncompete clause is a material constraint for a competing renderer.
Adding it as a required user-installed mod also conflicts with Fe2O3's stated
dependency limit. Do not copy its implementation. Any API-only integration
would still require the extra mod and is not selected.

Sources reviewed on 2026-10-10:

- https://github.com/wgpu-mc/wgpu-mc
- https://github.com/wgpu-mc/wgpu-mc/releases
- https://github.com/PrliStrxs/Vulcanite
- https://github.com/PrliStrxs/Vulcanite/blob/main/mgf-impl-26.2/src/main/java/dev/mgf/impl/vk/VkInteropImpl.java
- https://github.com/PrliStrxs/Vulcanite/blob/main/mgf-impl-26.2/src/main/java/dev/mgf/impl/provider/ProviderFrameBridge.java
- https://github.com/PrliStrxs/Vulcanite/blob/main/docs/compute-synchronization.md
- https://github.com/PrliStrxs/Vulcanite/blob/main/docs/resource-lifecycle.md
- https://github.com/PrliStrxs/Vulcanite/blob/main/LICENSE
- https://docs.rs/wgpu/26.0.1/wgpu/struct.Adapter.html

## Decision

No candidate currently provides a validated, compatible WebGPU path for sharing
Minecraft 26.2's active device, world-pass targets, and frame submission under
Fe2O3's dependency and distribution constraints.

Treat Vulcanite as evidence that a Vulkan-native provider seam exists for
final-frame color processing on 26.2. It does not meet M2's first world-pass
requirement and does not establish wgpu interoperation. Do not add a runtime
dependency, copy its implementation, pass Vulkan handles into Fe2O3's existing
independent wgpu device, or claim GPU interop from matching adapter names.

Keep Fe2O3's current rendering fallback and lifecycle support. Reconsider an
integration only after a supported path demonstrates that wgpu can safely record
work against the live Blaze3D device and targets and insert that work into the
same vanilla submission timeline, or after an upstream community backend provides
a compatible, distributable 26.2 Blaze3D backend on acceptable terms.

## Validation required to reopen this decision

1. Pin the exact upstream API/backend version and verify Minecraft 26.2/Fabric
   build compatibility and acceptable licensing/distribution.
2. Prove adapter identity and device ownership; obtain a nonzero physical-device
   identity and negotiate every required feature/extension before device creation.
3. Import or wrap the actual main/world target with exact format, usage, layout,
   dimensions and callback-scoped lifetime. The target must be a world-only image
   if the first replaced pass is world rendering.
4. Record WebGPU commands without an independent submission, enqueue them in
   Minecraft's pending graphics frame, and prove barriers, queue ordering and
   frame completion on Vulkan validation layers.
5. Run a packaged Fabric 26.2 client test covering known pixels, vanilla
   rollback, resize, reload, device recreation, shutdown, OpenGL fallback and
   Vulkan execution on hardware. Compare the same scene against vanilla.

Until all gates pass, a world-pass replacement is blocked. A diagnostic overlay,
copy/readback path, or final-composite hook is not evidence that terrain rendering
has been replaced.
