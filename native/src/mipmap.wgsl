// Exact 26.2 MEAN arithmetic. Tables come from the running game's ARGB API.
struct Dimensions { width: u32, height: u32, _pad0: u32, _pad1: u32 }
@group(0) @binding(0) var<storage, read> source: array<u32>;
@group(0) @binding(1) var<storage, read_write> destination: array<u32>;
@group(0) @binding(2) var<storage, read> tables: array<u32>;
@group(0) @binding(3) var<uniform> dimensions: Dimensions;

fn mean_channel(a: u32, b: u32, c: u32, d: u32, shift: u32) -> u32 {
    let sum = tables[(a >> shift) & 255u] + tables[(b >> shift) & 255u]
        + tables[(c >> shift) & 255u] + tables[(d >> shift) & 255u];
    return tables[256u + sum / 4u];
}

@compute @workgroup_size(8, 8)
fn main(@builtin(global_invocation_id) id: vec3<u32>) {
    let width = dimensions.width / 2u;
    let height = dimensions.height / 2u;
    if (id.x >= width || id.y >= height) { return; }
    let offset = id.y * 2u * dimensions.width + id.x * 2u;
    let a = source[offset];
    let b = source[offset + 1u];
    let c = source[offset + dimensions.width];
    let d = source[offset + dimensions.width + 1u];
    let alpha = ((a >> 24u) + (b >> 24u) + (c >> 24u) + (d >> 24u)) / 4u;
    destination[id.y * width + id.x] = (alpha << 24u)
        | (mean_channel(a, b, c, d, 16u) << 16u)
        | (mean_channel(a, b, c, d, 8u) << 8u)
        | mean_channel(a, b, c, d, 0u);
}

