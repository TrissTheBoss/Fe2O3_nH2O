package dev.fe2o3;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** Persistent user controls shared by the Sodium page and the mipmap adapter. */
public final class MipmapConfig {
    public static final int MINIMUM_PIXELS = 4;
    public static final int MAXIMUM_PIXELS = 65_536;
    private static final Logger LOGGER = LoggerFactory.getLogger("Fe2O3_nH2O");
    private static boolean enabled = Boolean.parseBoolean(System.getProperty("fe2o3.enabled", "true"));
    private static int minimumPixels = clamp(Integer.getInteger("fe2o3.minPixels", 4096));

    static { load(); }

    private MipmapConfig() { }

    public static synchronized boolean enabled() { return enabled; }
    public static synchronized int minimumPixels() { return minimumPixels; }

    public static synchronized void setEnabled(boolean value) { enabled = value; }
    public static synchronized void setMinimumPixels(int value) { minimumPixels = clamp(value); }

    /** Persist values after Sodium applies its staged option changes. */
    public static synchronized void save() {
        Properties properties = new Properties();
        properties.setProperty("mipmaps.enabled", Boolean.toString(enabled));
        properties.setProperty("mipmaps.minimumPixels", Integer.toString(minimumPixels));
        Path file = configFile();
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                properties.store(writer, "Fe2O3_nH2O mipmap settings");
            }
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException unsupportedAtomicMove) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException error) {
            LOGGER.error("Could not save mipmap settings to {}", file, error);
        }
    }

    private static void load() {
        Path file = configFile();
        if (!Files.isRegularFile(file)) return;
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            properties.load(reader);
            enabled = Boolean.parseBoolean(properties.getProperty("mipmaps.enabled", Boolean.toString(enabled)));
            minimumPixels = clamp(Integer.parseInt(properties.getProperty("mipmaps.minimumPixels",
                    Integer.toString(minimumPixels))));
        } catch (IOException | IllegalArgumentException error) {
            LOGGER.warn("Could not read mipmap settings from {}; using defaults", file, error);
        }
    }

    private static Path configFile() {
        try {
            return FabricLoader.getInstance().getConfigDir().resolve("fe2o3_nh2o.properties");
        } catch (RuntimeException | LinkageError unavailableLoader) {
            // Unit tests exercise the adapter without bootstrapping Fabric Loader.
            return Path.of("build", "test-config", "fe2o3_nh2o.properties");
        }
    }

    private static int clamp(int value) { return Math.clamp(value, MINIMUM_PIXELS, MAXIMUM_PIXELS); }
}
