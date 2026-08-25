package com.juiceybeans.eeexpanded.init;

import com.google.common.collect.Maps;
import com.juiceybeans.eeexpanded.client.renderer.CinderRenderer;
import com.juiceybeans.eeexpanded.client.renderer.EyestalkerRenderer;
import com.juiceybeans.eeexpanded.client.renderer.GallantRenderer;
import com.juiceybeans.eeexpanded.core.DeferredObject;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public class EEERenderers {
    private static final Map<DeferredObject<EntityType<? extends Entity>>, EntityRendererProvider<?>> providers = Maps.newHashMap();

    private EEERenderers() {
    }

    public static void registerRenderers() {
        registerEntityRender(EEEntities.GALLANT, GallantRenderer::new);
        registerEntityRender(EEEntities.CINDER, CinderRenderer::new);
        registerEntityRender(EEEntities.EYESTALKER, EyestalkerRenderer::new);
    }

    public static Map<DeferredObject<EntityType<? extends Entity>>, EntityRendererProvider<?>> getRenderers() {
        return providers;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> void registerEntityRender(DeferredObject<EntityType<T>> entityType, EntityRendererProvider provider) {
        providers.put((DeferredObject<EntityType<? extends Entity>>) (Object) entityType, provider);
    }
}
