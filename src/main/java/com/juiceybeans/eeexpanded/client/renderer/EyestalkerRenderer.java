package com.juiceybeans.eeexpanded.client.renderer;

import com.juiceybeans.eeexpanded.entity.EyestalkerEntity;
import com.juiceybeans.eeexpanded.init.EEEntities;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EyestalkerRenderer extends GeoEntityRenderer<EyestalkerEntity> {

    public EyestalkerRenderer(EntityRendererProvider.Context context) {
        super(context, EEEntities.EYESTALKER.get());
        this.shadowRadius = 0.5F;
        this.withScale(1.5f);
    }
}
