package com.juiceybeans.eeexpanded.client.renderer;

import com.juiceybeans.eeexpanded.entity.CinderEntity;
import com.juiceybeans.eeexpanded.init.EEEntities;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class CinderRenderer extends GeoEntityRenderer<CinderEntity> {
    public CinderRenderer(EntityRendererProvider.Context context) {
        super(context, EEEntities.CINDER.get());
        this.shadowRadius = 0.5F;
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public RenderType getRenderType(CinderEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
