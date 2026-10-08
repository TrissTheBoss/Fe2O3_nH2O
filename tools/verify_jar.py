"""Check the distributable, not just Gradle's source tree."""
import hashlib
import json
import pathlib
import shutil
import zipfile

artifacts = [p for p in pathlib.Path("build/libs").glob("*.jar") if not p.name.endswith("-sources.jar") and "gametest" not in p.name]
if len(artifacts) != 1:
    raise SystemExit(f"Expected one production JAR, got {artifacts}")
path = artifacts[0]
with zipfile.ZipFile(path) as jar:
    names = set(jar.namelist())
    metadata = json.loads(jar.read("fabric.mod.json"))
    assert metadata["depends"]["minecraft"] == "26.2"
    assert metadata["environment"] == "client"
    assert "${version}" not in metadata["version"]
    assert "dev/fe2o3/mixin/MipmapGeneratorMixin.class" in names
    assert "dev/fe2o3/ClientSmokeTest.class" not in names
    for platform, library in {
        "linux-x86_64": "libfe2o3_native.so",
        "windows-x86_64": "fe2o3_native.dll",
        "macos-aarch64": "libfe2o3_native.dylib",
        "macos-x86_64": "libfe2o3_native.dylib",
    }.items():
        name = f"natives/{platform}/{library}"
        assert hashlib.sha256(jar.read(name)).hexdigest() == jar.read(name + ".sha256").decode().strip()
    assert len(jar.read("natives/licenses/third-party.html")) > 1000
digest = hashlib.sha256(path.read_bytes()).hexdigest()
path.with_suffix(".jar.sha256").write_text(f"{digest}  {path.name}\n", encoding="ascii")
distribution = pathlib.Path("build/distributions")
distribution.mkdir(parents=True, exist_ok=True)
shutil.copyfile(path, distribution / path.name)
shutil.copyfile(path.with_suffix(".jar.sha256"), distribution / (path.name + ".sha256"))
print(f"Verified metadata, production/test isolation, four native payloads and notices: {path}")
print(f"SHA-256: {digest}")

