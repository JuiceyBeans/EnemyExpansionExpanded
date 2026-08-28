package com.juiceybeans.eeexpanded.client.model;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.entity.PropellerEntity;

import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.model.GeoModel;

public class PropellerModel extends GeoModel<PropellerEntity> {

    public PropellerModel() {}

    @Override
    public ResourceLocation getModelResource(PropellerEntity propellerEntity) {
        return EEExpanded.id("geo/entity/propeller.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PropellerEntity propellerEntity) {
        return propellerEntity.getWeathered() ? EEExpanded.id("textures/entity/propeller_weathered.png") :
                EEExpanded.id("textures/entity/propeller.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PropellerEntity propellerEntity) {
        return EEExpanded.id("animations/entity/propeller.animation.json");
    }
}
