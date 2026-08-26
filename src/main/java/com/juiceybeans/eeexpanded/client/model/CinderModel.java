package com.juiceybeans.eeexpanded.client.model;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.entity.CinderEntity;

import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.model.GeoModel;

public class CinderModel extends GeoModel<CinderEntity> {

    @Override
    public ResourceLocation getModelResource(CinderEntity cinderEntity) {
        return EEExpanded.id("geo/entity/cinder.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CinderEntity cinderEntity) {
        return EEExpanded.id("textures/entity/cinder");
    }

    @Override
    public ResourceLocation getAnimationResource(CinderEntity cinderEntity) {
        return EEExpanded.id("animations/entity/cinder.animation.json");
    }
}
