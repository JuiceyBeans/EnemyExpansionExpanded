package com.juiceybeans.eeexpanded.util;

import net.minecraft.world.entity.Entity;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

public class AnimationUtils {

    public static <T extends Entity & GeoEntity> AnimationController<GeoAnimatable> getController(T entity,
                                                                                                  String controller) {
        return entity.getAnimatableInstanceCache().getManagerForId(entity.getId()).getAnimationControllers()
                .get(controller);
    }

    public static boolean isCurrentAnimation(AnimationController<GeoAnimatable> controller, RawAnimation animation) {
        return controller.getCurrentRawAnimation() == animation;
    }

    public static <T extends Entity & GeoEntity> boolean isCurrentAnimation(T entity, String controller,
                                                                            RawAnimation animation) {
        return getController(entity, controller).getCurrentRawAnimation() == animation;
    }

    public static boolean isControllerPlaying(AnimationController<GeoAnimatable> controller) {
        return controller.getAnimationState() == AnimationController.State.RUNNING;
    }

    public static <T extends Entity & GeoEntity> boolean isControllerPlaying(T entity, String controller) {
        return getController(entity, controller).getAnimationState() == AnimationController.State.RUNNING;
    }
}
