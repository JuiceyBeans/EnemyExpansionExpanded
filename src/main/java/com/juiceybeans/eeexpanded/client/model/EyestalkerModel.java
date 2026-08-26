package com.juiceybeans.eeexpanded.client.model;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.entity.EyestalkerEntity;

import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.model.GeoModel;

public class EyestalkerModel extends GeoModel<EyestalkerEntity> {

    @Override
    public ResourceLocation getModelResource(EyestalkerEntity eyestalkerEntity) {
        return EEExpanded.id("geo/entity/eyestalker.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EyestalkerEntity gallantEntity) {
        return EEExpanded.id("textures/entity/eyestalker");
    }

    @Override
    public ResourceLocation getAnimationResource(EyestalkerEntity gallantEntity) {
        return EEExpanded.id("animations/entity/eyestalker.animation.json");
    }
}
