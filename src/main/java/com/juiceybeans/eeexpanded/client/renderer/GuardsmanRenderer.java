package com.juiceybeans.eeexpanded.client.renderer;

import com.juiceybeans.eeexpanded.entity.GuardsmanEntity;
import com.juiceybeans.eeexpanded.init.EEEntities;

import net.minecraft.client.renderer.entity.EntityRendererProvider;

import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GuardsmanRenderer extends GeoEntityRenderer<GuardsmanEntity> {

    public GuardsmanRenderer(EntityRendererProvider.Context context) {
        super(context, EEEntities.GUARDSMAN.get());
        this.shadowRadius = 0.5F;
    }
}
