package com.juiceybeans.eeexpanded.client.renderer;

import com.juiceybeans.eeexpanded.entity.GallantEntity;
import com.juiceybeans.eeexpanded.init.EEEntities;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GallantRenderer extends GeoEntityRenderer<GallantEntity> {
    public GallantRenderer(EntityRendererProvider.Context context) {
        super(context, EEEntities.GALLANT.get());
        this.shadowRadius = 0.6F;
    }

    @Override
    public RenderType getRenderType(GallantEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
