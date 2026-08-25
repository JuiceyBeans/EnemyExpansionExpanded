package com.juiceybeans.eeexpanded.client.renderer.layer;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.entity.CinderEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class CinderRenderLayer extends GeoRenderLayer<CinderEntity> {
    private static final ResourceLocation EYES = EEExpanded.id("textures/entity/cinder_glowmask.png");
    private static final ResourceLocation MODEL = EEExpanded.id("geo/entity/cinder.geo.json");

    public CinderRenderLayer(GeoRenderer<CinderEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, CinderEntity animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        var cameo = RenderType.eyes(EYES);
        poseStack.pushPose();
        poseStack.popPose();
    }
}

