"""Stage a locally compiled native and its digest for Gradle; no network access."""
import hashlib
import pathlib
import platform
import shutil

root = pathlib.Path(__file__).resolve().parents[1]
systems = {"Linux": ("linux", "libfe2o3_native.so"), "Windows": ("windows", "fe2o3_native.dll"), "Darwin": ("macos", "libfe2o3_native.dylib")}
system, filename = systems[platform.system()]
arch = {"AMD64": "x86_64", "x86_64": "x86_64", "arm64": "aarch64", "aarch64": "aarch64"}[platform.machine()]
source = root / "native" / "target" / "release" / filename
target = root / "build" / "native" / f"{system}-{arch}" / filename
target.parent.mkdir(parents=True, exist_ok=True)
shutil.copyfile(source, target)
target.with_name(filename + ".sha256").write_text(hashlib.sha256(target.read_bytes()).hexdigest() + "\n", encoding="ascii")
print(f"Staged {target.relative_to(root)}")

