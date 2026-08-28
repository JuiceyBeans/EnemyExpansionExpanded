package com.juiceybeans.eeexpanded.client.renderer;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.entity.PropellerEntity;
import com.juiceybeans.eeexpanded.init.EEEntities;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PropellerRenderer extends GeoEntityRenderer<PropellerEntity> {

    public PropellerRenderer(EntityRendererProvider.Context context) {
        super(context, EEEntities.PROPELLER.get());
        this.shadowRadius = 0.4F;
    }

    @Override
    public ResourceLocation getTextureLocation(PropellerEntity animatable) {
        return animatable.getWeathered() ? EEExpanded.id("textures/entity/propeller_weathered.png") :
                EEExpanded.id("textures/entity/propeller.png");
    }

    @Override
    public RenderType getRenderType(PropellerEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
