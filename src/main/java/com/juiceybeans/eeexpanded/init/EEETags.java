package com.juiceybeans.eeexpanded.init;

import com.juiceybeans.eeexpanded.EEExpanded;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class EEETags {

    public static final TagKey<EntityType<?>> CANNOT_DISMOUNT = createEntityTag("cannot_dismount");

    private static TagKey<EntityType<?>> createEntityTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, EEExpanded.id(name));
    }
}
