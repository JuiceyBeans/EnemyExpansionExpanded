package com.juiceybeans.eeexpanded.client.model;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.entity.GuardsmanEntity;

import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.model.GeoModel;

public class GuardsmanModel extends GeoModel<GuardsmanEntity> {

    public GuardsmanModel() {}

    @Override
    public ResourceLocation getModelResource(GuardsmanEntity guardsmanEntity) {
        return EEExpanded.id("geo/entity/guardsman.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GuardsmanEntity guardsmanEntity) {
        return EEExpanded.id("textures/entity/guardsman");
    }

    @Override
    public ResourceLocation getAnimationResource(GuardsmanEntity guardsmanEntity) {
        return EEExpanded.id("animations/entity/guardsman.animation.json");
    }
}
