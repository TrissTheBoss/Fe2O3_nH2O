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


## Direct Vulkan bridge spike assessment — 2026-10-10

A source-level 26.2 assessment found a possible *instrumentation seam*, not yet a safe WebGPU bridge. The vanilla Vulkan backend's device object exposes accessors for its Vulkan instance, logical device, graphics queue and command encoder; its Vulkan command encoder has an operation to enqueue a raw Vulkan command buffer before Minecraft submits. However, Fe2O3 currently receives only the public `GpuDevice`; its backend reference is private, so reaching those implementation classes requires version-specific mixin/accessor code. The Vulkan device object closes the `VulkanPhysicalDevice` object after using it to populate device info and queue objects, and does not retain the physical-device handle or the logical-device creation feature chain as public state.

This is a material issue for the pinned HAL. In the exact wgpu-hal v26.0.6 source, Vulkan `Adapter::device_from_raw` requires that the raw logical device was created from that exact HAL adapter and with the specified queue family, enabled extensions, and physical-device features; it also imposes explicit lifetime/ownership rules. In VulkanBackend's source, the `vkCreateDevice` call uses a private required-feature list and local creation structures. A bridge would therefore need to instrument device creation to capture exact instance/physical-device identity, enabled queue families, extension names and enabled feature chain, and retain matching lifetimes. A device-name or vendor/device-ID match is insufficient to prove physical-device identity.

The command-submission side also remains incomplete: Minecraft can enqueue raw Vulkan command buffers, but Fe2O3's pinned public wgpu `CommandBuffer` is opaque and its public queue submits through its own `Queue::submit`. No pinned public path has been established to obtain the Vulkan command buffer recorded by wgpu and enqueue it in Minecraft's pending encoder. Therefore the raw-handle seam is promising for a low-level Vulkan prototype, but does not currently provide a WebGPU-to-Blaze3D bridge.

### Spike acceptance criteria

1. Add a disposable Vulkan-only client experiment behind an explicit development flag; do not alter production rendering or default behavior.
2. Instrument backend creation and record the exact Vulkan instance, physical-device, logical-device, graphics queue family/index, enabled extensions and enabled Vulkan feature chain without taking ownership.
3. Using the exact pinned HAL API, prove adapter/device identity and that all safety requirements for imported device and borrowed handles can be satisfied. Abort on incomplete metadata; never guess identity from adapter strings.
4. Record a no-op or buffer-only WebGPU workload and enqueue its command buffer into Minecraft's existing frame submission without an independent queue submit.
5. Run the experiment on validation-enabled software Vulkan and one hardware Vulkan path; verify correct output, synchronization, resize and teardown. Keep the vanilla renderer active.

If the pinned wgpu API cannot expose a recordable raw command buffer while preserving wgpu resource tracking, stop the spike and evaluate a supported upstream API change or alternate renderer architecture. Do not replace a world pass based only on raw Vulkan access.

Source references reviewed:

- https://github.com/Renekovski/26.2-mcp/blob/main/src/com/mojang/blaze3d/systems/GpuDevice.java
- https://github.com/Renekovski/26.2-mcp/blob/main/src/com/mojang/blaze3d/vulkan/VulkanBackend.java
- https://github.com/Renekovski/26.2-mcp/blob/main/src/com/mojang/blaze3d/vulkan/VulkanDevice.java
- https://github.com/Renekovski/26.2-mcp/blob/main/src/com/mojang/blaze3d/vulkan/VulkanCommandEncoder.java
- https://github.com/gfx-rs/wgpu/blob/v26.0.6/wgpu-hal/src/vulkan/adapter.rs
- https://docs.rs/wgpu/26.0.1/wgpu/struct.CommandBuffer.html
