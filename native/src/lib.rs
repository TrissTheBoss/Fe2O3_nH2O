//! Native WebGPU mipmap stage. No Minecraft pointers cross this boundary.
use jni::objects::{JClass, JIntArray};
use jni::sys::{jint, jintArray};
use jni::JNIEnv;
use std::panic::{catch_unwind, AssertUnwindSafe};
use std::sync::{Mutex, OnceLock};
use std::time::{Duration, Instant};
use wgpu::util::DeviceExt;

const MAX_SIDE: u32 = 2048;
static RENDERER: OnceLock<Mutex<Option<Renderer>>> = OnceLock::new();

fn chain_sizes(width: u32, height: u32, levels: u32) -> Result<Vec<(u32, u32)>, String> {
    if width == 0
        || height == 0
        || width > MAX_SIDE
        || height > MAX_SIDE
        || !(1..=11).contains(&levels)
    {
        return Err("dimensions or mip count out of bounds".into());
    }
    let (mut w, mut h) = (width, height);
    let mut sizes = Vec::new();
    for _ in 0..levels {
        w /= 2;
        h /= 2;
        if w == 0 || h == 0 {
            return Err("mip dimensions would become zero".into());
        }
        sizes.push((w, h));
    }
    Ok(sizes)
}

struct Renderer {
    device: wgpu::Device,
    queue: wgpu::Queue,
    pipeline: wgpu::ComputePipeline,
    tables: wgpu::Buffer,
}

impl Renderer {
    fn new(tables: &[i32]) -> Result<Self, String> {
        if tables.len() != 1280
            || tables[..256].iter().any(|&v| !(0..=1023).contains(&v))
            || tables[256..].iter().any(|&v| !(0..=255).contains(&v))
        {
            return Err("invalid color tables".into());
        }
        let instance = wgpu::Instance::default();
        let adapter = pollster::block_on(instance.request_adapter(&wgpu::RequestAdapterOptions {
            power_preference: wgpu::PowerPreference::LowPower,
            ..Default::default()
        }))
        .map_err(|e| e.to_string())?;
        let (device, queue) = pollster::block_on(adapter.request_device(&wgpu::DeviceDescriptor {
            label: Some("Fe2O3 mipmaps"),
            required_features: wgpu::Features::empty(),
            required_limits: wgpu::Limits::downlevel_defaults(),
            memory_hints: wgpu::MemoryHints::MemoryUsage,
            trace: wgpu::Trace::Off,
        }))
        .map_err(|e| e.to_string())?;
        eprintln!("[Fe2O3] WebGPU adapter: {:?}", adapter.get_info());
        let module = device.create_shader_module(wgpu::include_wgsl!("mipmap.wgsl"));
        let pipeline = device.create_compute_pipeline(&wgpu::ComputePipelineDescriptor {
            label: Some("Fe2O3 exact MEAN mipmaps"),
            layout: None,
            module: &module,
            entry_point: Some("main"),
            compilation_options: Default::default(),
            cache: None,
        });
        let tables = device.create_buffer_init(&wgpu::util::BufferInitDescriptor {
            label: Some("Minecraft color tables"),
            contents: bytemuck::cast_slice(tables),
            usage: wgpu::BufferUsages::STORAGE,
        });
        Ok(Self {
            device,
            queue,
            pipeline,
            tables,
        })
    }

    fn generate(
        &self,
        input: &[i32],
        width: u32,
        height: u32,
        levels: u32,
    ) -> Result<Vec<i32>, String> {
        let sizes = chain_sizes(width, height, levels)?;
        if input.len() != (width * height) as usize {
            return Err("pixel count does not match dimensions".into());
        }
        let size = sizes.iter().map(|(w, h)| u64::from(w * h) * 4).sum();
        let readback = self.device.create_buffer(&wgpu::BufferDescriptor {
            label: Some("Fe2O3 chain readback"),
            size,
            usage: wgpu::BufferUsages::COPY_DST | wgpu::BufferUsages::MAP_READ,
            mapped_at_creation: false,
        });
        let mut previous = self
            .device
            .create_buffer_init(&wgpu::util::BufferInitDescriptor {
                label: Some("Fe2O3 source pixels"),
                contents: bytemuck::cast_slice(input),
                usage: wgpu::BufferUsages::STORAGE,
            });
        let mut encoder = self.device.create_command_encoder(&Default::default());
        let (mut w, mut h, mut offset) = (width, height, 0);
        for (next_w, next_h) in sizes {
            let bytes = u64::from(next_w * next_h) * 4;
            let output = self.device.create_buffer(&wgpu::BufferDescriptor {
                label: Some("Fe2O3 mip level"),
                size: bytes,
                usage: wgpu::BufferUsages::STORAGE | wgpu::BufferUsages::COPY_SRC,
                mapped_at_creation: false,
            });
            let dimensions = self
                .device
                .create_buffer_init(&wgpu::util::BufferInitDescriptor {
                    label: Some("Fe2O3 dimensions"),
                    contents: bytemuck::cast_slice(&[w, h, 0, 0]),
                    usage: wgpu::BufferUsages::UNIFORM,
                });
            let bindings = self.device.create_bind_group(&wgpu::BindGroupDescriptor {
                label: Some("Fe2O3 mip bindings"),
                layout: &self.pipeline.get_bind_group_layout(0),
                entries: &[
                    wgpu::BindGroupEntry {
                        binding: 0,
                        resource: previous.as_entire_binding(),
                    },
                    wgpu::BindGroupEntry {
                        binding: 1,
                        resource: output.as_entire_binding(),
                    },
                    wgpu::BindGroupEntry {
                        binding: 2,
                        resource: self.tables.as_entire_binding(),
                    },
                    wgpu::BindGroupEntry {
                        binding: 3,
                        resource: dimensions.as_entire_binding(),
                    },
                ],
            });
            {
                let mut pass = encoder.begin_compute_pass(&wgpu::ComputePassDescriptor {
                    label: Some("Fe2O3 mipmap"),
                    timestamp_writes: None,
                });
                pass.set_pipeline(&self.pipeline);
                pass.set_bind_group(0, &bindings, &[]);
                pass.dispatch_workgroups(next_w.div_ceil(8), next_h.div_ceil(8), 1);
            }
            encoder.copy_buffer_to_buffer(&output, 0, &readback, offset, bytes);
            previous = output;
            (w, h) = (next_w, next_h);
            offset += bytes;
        }
        self.queue.submit([encoder.finish()]);
        let slice = readback.slice(..);
        let (sender, receiver) = std::sync::mpsc::sync_channel(1);
        slice.map_async(wgpu::MapMode::Read, move |r| {
            let _ = sender.send(r);
        });
        let deadline = Instant::now() + Duration::from_secs(10);
        loop {
            self.device
                .poll(wgpu::PollType::Poll)
                .map_err(|e| e.to_string())?;
            match receiver.try_recv() {
                Ok(result) => {
                    result.map_err(|e| e.to_string())?;
                    break;
                }
                Err(std::sync::mpsc::TryRecvError::Empty) if Instant::now() < deadline => {
                    std::thread::sleep(Duration::from_millis(1))
                }
                _ => return Err("GPU readback failed or exceeded 10 seconds".into()),
            }
        }
        let result = bytemuck::cast_slice::<u8, i32>(&slice.get_mapped_range()).to_vec();
        readback.unmap();
        Ok(result)
    }
}

fn read_ints(env: &JNIEnv, array: &JIntArray, max: usize) -> Result<Vec<i32>, String> {
    let len = env.get_array_length(array).map_err(|e| e.to_string())? as usize;
    if len > max {
        return Err("JNI array too large".into());
    }
    let mut data = vec![0; len];
    env.get_int_array_region(array, 0, &mut data)
        .map_err(|e| e.to_string())?;
    Ok(data)
}

fn guarded<T>(env: &mut JNIEnv, f: impl FnOnce(&mut JNIEnv) -> Result<T, String>) -> Option<T> {
    let result = catch_unwind(AssertUnwindSafe(|| f(env)))
        .unwrap_or_else(|_| Err("native renderer panicked; disabling WebGPU".into()));
    match result {
        Ok(value) => Some(value),
        Err(error) => {
            if !env.exception_check().unwrap_or(true) {
                let _ = env.throw_new("java/lang/IllegalStateException", error);
            }
            None
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_dev_fe2o3_NativeBridge_initialize(
    mut env: JNIEnv,
    _: JClass,
    tables: JIntArray,
) {
    guarded(&mut env, |env| {
        let data = read_ints(env, &tables, 1280)?;
        let mut guard = RENDERER
            .get_or_init(|| Mutex::new(None))
            .lock()
            .map_err(|e| e.to_string())?;
        if guard.is_none() {
            *guard = Some(Renderer::new(&data)?);
        }
        Ok(())
    });
}

#[no_mangle]
pub extern "system" fn Java_dev_fe2o3_NativeBridge_generate(
    mut env: JNIEnv,
    _: JClass,
    pixels: JIntArray,
    width: jint,
    height: jint,
    levels: jint,
) -> jintArray {
    guarded(&mut env, |env| {
        chain_sizes(width as u32, height as u32, levels as u32)?;
        let input = read_ints(env, &pixels, (MAX_SIDE * MAX_SIDE) as usize)?;
        let guard = RENDERER
            .get_or_init(|| Mutex::new(None))
            .lock()
            .map_err(|e| e.to_string())?;
        let renderer = guard.as_ref().ok_or("renderer is not initialized")?;
        let output = renderer.generate(&input, width as u32, height as u32, levels as u32)?;
        let result = env
            .new_int_array(output.len() as i32)
            .map_err(|e| e.to_string())?;
        env.set_int_array_region(&result, 0, &output)
            .map_err(|e| e.to_string())?;
        Ok(result.into_raw())
    })
    .unwrap_or(std::ptr::null_mut())
}

#[no_mangle]
pub extern "system" fn Java_dev_fe2o3_NativeBridge_shutdown(mut env: JNIEnv, _: JClass) {
    guarded(&mut env, |_| {
        if let Some(renderer) = RENDERER.get() {
            *renderer.lock().map_err(|e| e.to_string())? = None;
        }
        Ok(())
    });
}

#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn rejects_invalid_shapes_before_allocating() {
        for (w, h, l) in [
            (0, 8, 1),
            (4096, 8, 1),
            (8, 8, 0),
            (8, 8, 4),
            (8, 8, u32::MAX),
        ] {
            assert!(chain_sizes(w, h, l).is_err());
        }
    }
    #[test]
    fn rectangular_and_odd_shapes_follow_vanilla_flooring() {
        assert_eq!(
            chain_sizes(15, 10, 3).unwrap(),
            vec![(7, 5), (3, 2), (1, 1)]
        );
        assert_eq!(chain_sizes(2048, 2048, 11).unwrap().last(), Some(&(1, 1)));
    }
}
