package com.juiceybeans.eeexpanded.core;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface RegisterFunction<T> {
    /**
     * Register a single object into the supplied {@link Registry} under the given {@link ResourceLocation}.
     * <p>
     * Implementations should bridge to the platform-specific registration call (Forge/Fabric or other)
     * and ensure the provided object is registered under the supplied id.
     *
     * @param registry the {@link Registry} to register into, may be {@code null} when the registry instance
     *                 is not required by the caller
     * @param id       the {@link ResourceLocation} to register the object under
     * @param obj      the object instance to register
     */
    void register(Registry<T> registry, ResourceLocation id, T obj);
}
