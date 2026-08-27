package com.juiceybeans.eeexpanded.effect;

import com.juiceybeans.eeexpanded.init.EEEMobEffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.phys.Vec3;

public class GroundBoundEffect extends MobEffect {

    public GroundBoundEffect() {
        super(MobEffectCategory.HARMFUL, 13434778);
    }

    @Override
    public String getDescriptionId() {
        return "effect.eeexpanded.ground_bound";
    }

    @Override
    public void addAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int amplifier) {
        livingEntity
                .setDeltaMovement(
                        new Vec3(0.0,
                                -0.25 * (livingEntity.hasEffect(EEEMobEffects.GROUND_BOUND.get()) ?
                                        livingEntity.getEffect(EEEMobEffects.GROUND_BOUND.get()).getAmplifier() : 0),
                                0.0));
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        livingEntity
                .setDeltaMovement(
                        new Vec3(0.0,
                                -0.25 * (livingEntity.hasEffect(EEEMobEffects.GROUND_BOUND.get()) ?
                                        livingEntity.getEffect(EEEMobEffects.GROUND_BOUND.get()).getAmplifier() : 0),
                                0.0));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
