package com.juiceybeans.eeexpanded;

import com.juiceybeans.eeexpanded.init.EEERenderers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class EEExpandedClient {

    @SuppressWarnings("unchecked")
    public static void clientSetup(final FMLClientSetupEvent event) {
        EEERenderers.registerRenderers();
        EEERenderers.getRenderers().forEach((entity, renderer) -> EntityRenderers.register(entity.get(),
                (EntityRendererProvider<Entity>) renderer));
    }
}
