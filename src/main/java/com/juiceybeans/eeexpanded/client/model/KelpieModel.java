package com.juiceybeans.eeexpanded.client.model;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.entity.KelpieEntity;

import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.model.GeoModel;

public class KelpieModel extends GeoModel<KelpieEntity> {

    public KelpieModel() {}

    @Override
    public ResourceLocation getModelResource(KelpieEntity kelpieEntity) {
        return EEExpanded.id("geo/entity/kelpie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KelpieEntity kelpieEntity) {
        return EEExpanded.id("textures/entity/kelpie");
    }

    @Override
    public ResourceLocation getAnimationResource(KelpieEntity kelpieEntity) {
        return EEExpanded.id("animations/entity/kelpie.animation.json");
    }
}
