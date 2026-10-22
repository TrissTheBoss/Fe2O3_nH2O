package dev.fe2o3;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Registers Fe2O3's controls through Sodium's public Video Settings config API. */
public final class SodiumConfigEntryPoint implements ConfigEntryPoint {
    private static final Identifier ENABLED = Identifier.fromNamespaceAndPath("fe2o3_nh2o", "mipmaps_enabled");
    private static final Identifier MIN_PIXELS = Identifier.fromNamespaceAndPath("fe2o3_nh2o", "mipmaps_minimum_pixels");
    private final Runnable saveHandler = MipmapConfig::save;

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        builder.registerOwnModOptions()
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("fe2o3_nh2o.options.page.mipmaps"))
                        .addOptionGroup(builder.createOptionGroup()
                                .setName(Component.translatable("fe2o3_nh2o.options.group.acceleration"))
                                .addOption(builder.createBooleanOption(ENABLED)
                                        .setName(Component.translatable("fe2o3_nh2o.options.mipmaps.enabled"))
                                        .setTooltip(Component.translatable("fe2o3_nh2o.options.mipmaps.enabled.tooltip"))
                                        .setStorageHandler(saveHandler)
                                        .setBinding(MipmapConfig::setEnabled, MipmapConfig::enabled)
                                        .setDefaultValue(true))
                                .addOption(builder.createIntegerOption(MIN_PIXELS)
                                        .setName(Component.translatable("fe2o3_nh2o.options.mipmaps.minimum_pixels"))
                                        .setTooltip(Component.translatable("fe2o3_nh2o.options.mipmaps.minimum_pixels.tooltip"))
                                        .setStorageHandler(saveHandler)
                                        .setBinding(MipmapConfig::setMinimumPixels, MipmapConfig::minimumPixels)
                                        .setRange(MipmapConfig.MINIMUM_PIXELS, MipmapConfig.MAXIMUM_PIXELS, 4)
                                        .setValueFormatter(value -> Component.translatable(
                                                "fe2o3_nh2o.options.mipmaps.pixels", value))
                                        .setDefaultValue(4096))));
    }
}
