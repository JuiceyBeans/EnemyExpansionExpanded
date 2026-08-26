package com.juiceybeans.eeexpanded.client.model;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.entity.GallantEntity;

import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.model.GeoModel;

public class GallantModel extends GeoModel<GallantEntity> {

    @Override
    public ResourceLocation getModelResource(GallantEntity gallantEntity) {
        return EEExpanded.id("geo/entity/gallant.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GallantEntity gallantEntity) {
        return EEExpanded.id("textures/entity/gallant");
    }

    @Override
    public ResourceLocation getAnimationResource(GallantEntity gallantEntity) {
        return EEExpanded.id("animations/entity/gallant.animation.json");
    }
}
