package com.juiceybeans.eeexpanded.init;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.core.DeferredObject;
import com.juiceybeans.eeexpanded.core.RegisterFunction;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;

public class EEESoundEvents {

    static Map<String, DeferredObject<? extends SoundEvent>> soundEvents = new HashMap<>();

    public static final SoundEvent SILENT_STEP = SoundEvent
            .createVariableRangeEvent(EEExpanded.id("entity.silentstep"));
    public static final SoundEvent KELPIE_IDLE = SoundEvent
            .createVariableRangeEvent(EEExpanded.id("entity.kelpie.idle"));
    public static final SoundEvent KELPIE_REINFORCEMENT = SoundEvent
            .createVariableRangeEvent(EEExpanded.id("entity.kelpie.reinforcement"));

    public static void register(RegisterFunction<SoundEvent> function) {
        soundEvents.forEach(((id, soundEvent) -> function.register(
                BuiltInRegistries.SOUND_EVENT, EEExpanded.id(id), soundEvent.get())));
    }
}
