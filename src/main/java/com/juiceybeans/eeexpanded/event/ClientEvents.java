package com.juiceybeans.eeexpanded.event;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.client.model.*;
import com.juiceybeans.eeexpanded.init.EEEntities;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mod.EventBusSubscriber(modid = EEExpanded.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EEEntities.GALLANT.get(),
                (EntityRendererProvider.Context context) -> new GeoEntityRenderer<>(context, new GallantModel()));
        event.registerEntityRenderer(EEEntities.CINDER.get(),
                (EntityRendererProvider.Context context) -> new GeoEntityRenderer<>(context, new CinderModel()));
        event.registerEntityRenderer(EEEntities.EYESTALKER.get(),
                (EntityRendererProvider.Context context) -> new GeoEntityRenderer<>(context, new EyestalkerModel()));
        event.registerEntityRenderer(EEEntities.DRAGONFLY.get(),
                (EntityRendererProvider.Context context) -> new GeoEntityRenderer<>(context, new DragonflyModel()));
        event.registerEntityRenderer(EEEntities.GUARDSMAN.get(),
                (EntityRendererProvider.Context context) -> new GeoEntityRenderer<>(context, new GuardsmanModel()));

        event.registerEntityRenderer(EEEntities.GALLANT_SWINGS.get(), ThrownItemRenderer::new);
    }
}
