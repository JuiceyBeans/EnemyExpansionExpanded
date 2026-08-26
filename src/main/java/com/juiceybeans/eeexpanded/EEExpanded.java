package com.juiceybeans.eeexpanded;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod(EEExpanded.MOD_ID)
public class EEExpanded {

    public static final String MOD_ID = "eeexpanded";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EEExpanded() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        if (FMLEnvironment.dist.isClient()) {
            bus.addListener(EEExpandedClient::clientSetup);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
