package dev.fe2o3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

final class NativeLoader {
    private NativeLoader() { }

    static String platform(String os, String arch) {
        os = os.toLowerCase(Locale.ROOT);
        String cpu = switch (arch.toLowerCase(Locale.ROOT)) {
            case "amd64", "x86_64" -> "x86_64";
            case "aarch64", "arm64" -> "aarch64";
            default -> throw new IllegalStateException("Unsupported CPU: " + arch);
        };
        if (os.contains("win")) return "windows-" + cpu;
        if (os.contains("mac")) return "macos-" + cpu;
        if (os.contains("linux")) return "linux-" + cpu;
        throw new IllegalStateException("Unsupported OS: " + os);
    }

    static void load() throws IOException, NoSuchAlgorithmException {
        String platform = platform(System.getProperty("os.name"), System.getProperty("os.arch"));
        String file = System.mapLibraryName("fe2o3_native");
        String resource = "/natives/" + platform + "/" + file;
        byte[] binary;
        String expected;
        try (var stream = NativeLoader.class.getResourceAsStream(resource);
             var checksum = NativeLoader.class.getResourceAsStream(resource + ".sha256")) {
            if (stream == null || checksum == null) throw new IOException("No bundled native for " + platform);
            binary = stream.readNBytes(64 * 1024 * 1024 + 1);
            if (binary.length > 64 * 1024 * 1024) throw new IOException("Native exceeds size limit");
            expected = new String(checksum.readNBytes(128), StandardCharsets.US_ASCII).trim();
        }
        String actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(binary));
        if (!actual.equals(expected)) throw new IOException("Bundled native checksum mismatch");
        Path directory = Files.createTempDirectory("fe2o3-");
        directory.toFile().deleteOnExit();
        Path library = directory.resolve(file);
        library.toFile().deleteOnExit();
        Files.write(library, binary);
        System.load(library.toAbsolutePath().toString());
    }
}

