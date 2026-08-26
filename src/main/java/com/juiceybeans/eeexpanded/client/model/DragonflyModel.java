package com.juiceybeans.eeexpanded.client.model;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.entity.DragonflyEntity;
import com.juiceybeans.eeexpanded.entity.EyestalkerEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DragonflyModel extends GeoModel<DragonflyEntity> {
    @Override
    public ResourceLocation getModelResource(DragonflyEntity gallantEntity) {
        return EEExpanded.id("geo/entity/dragonfly.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DragonflyEntity gallantEntity) {
        return EEExpanded.id("textures/entity/dragonfly");
    }

    @Override
    public ResourceLocation getAnimationResource(DragonflyEntity dragonflyEntity) {
        return EEExpanded.id("animations/entity/dragonfly.animation.json");
    }
}