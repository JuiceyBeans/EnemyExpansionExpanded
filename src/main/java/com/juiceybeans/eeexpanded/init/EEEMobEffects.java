package com.juiceybeans.eeexpanded.init;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.core.DeferredObject;
import com.juiceybeans.eeexpanded.core.RegisterFunction;
import com.juiceybeans.eeexpanded.effect.GroundBoundEffect;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EEEMobEffects {

    static final Map<String, DeferredObject<? extends MobEffect>> effects = new HashMap<>();

    public static final DeferredObject<MobEffect> GROUND_BOUND = effect("ground_bound", GroundBoundEffect::new);

    private EEEMobEffects() {}

    public static void register(RegisterFunction<MobEffect> function) {
        effects.forEach(
                ((id, effect) -> function.register(BuiltInRegistries.MOB_EFFECT, EEExpanded.id(id), effect.get())));
    }

    static <T extends MobEffect> DeferredObject<T> effect(String name, Supplier<T> effectSupplier) {
        DeferredObject<T> ret = new DeferredObject<>(effectSupplier);
        effects.put(name, ret);
        return ret;
    }
}
