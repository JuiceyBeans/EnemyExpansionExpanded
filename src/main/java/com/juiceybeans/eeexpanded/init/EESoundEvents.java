package com.juiceybeans.eeexpanded.init;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.core.DeferredObject;
import com.juiceybeans.eeexpanded.core.RegisterFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EESoundEvents {
    static Map<String, DeferredObject<? extends SoundEvent>> soundEvents = new HashMap<>();

    public static final SoundEvent SILENT_STEP = SoundEvent.createVariableRangeEvent(EEExpanded.id("silentstep"));

    public static void register(RegisterFunction<SoundEvent> function) {
        soundEvents.forEach(((id, soundEvent) -> function.register(
                BuiltInRegistries.SOUND_EVENT, EEExpanded.id(id), soundEvent.get())));
    }
}
