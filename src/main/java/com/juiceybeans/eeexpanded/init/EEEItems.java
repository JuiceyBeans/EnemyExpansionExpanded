package com.juiceybeans.eeexpanded.init;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.core.DeferredObject;
import com.juiceybeans.eeexpanded.core.RegisterFunction;
import com.juiceybeans.eeexpanded.item.CrescentDiscItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EEEItems {

    static final Map<String, DeferredObject<? extends Item>> items = new HashMap<>(); // For register function

    public static final DeferredObject<Item> CRESCENT_DISC = item("crescent_disc",
            () -> new CrescentDiscItem(new Item.Properties()), true, true);

    public static final DeferredObject<Item> GALLANT_SPAWN_EGG = spawnEgg("gallant_spawn_egg", EEEntities.GALLANT.get(),
            10192776, 3332032);
    public static final DeferredObject<Item> CINDER_SPAWN_EGG = spawnEgg("cinder_spawn_egg", EEEntities.CINDER.get(),
            238848, 11132);
    public static final DeferredObject<Item> EYESTALKER_SPAWN_EGG = spawnEgg("eyestalker_spawn_egg",
            EEEntities.EYESTALKER.get(),
            15985632, 9788045);
    public static final DeferredObject<Item> DRAGONFLY_SPAWN_EGG = spawnEgg("dragonfly_spawn_egg",
            EEEntities.DRAGONFLY.get(),
            10117792, 5843863);
    public static final DeferredObject<Item> GUARDSMAN_SPAWN_EGG = spawnEgg("guardsman_spawn_egg",
            EEEntities.GUARDSMAN.get(),
            2894893, 9301753);
    public static final DeferredObject<Item> KELPIE_SPAWN_EGG = spawnEgg("kelpie_spawn_egg",
            EEEntities.KELPIE.get(),
            11899790, 14259169);
    public static final DeferredObject<Item> PROPELLER_SPAWN_EGG = spawnEgg("propeller_spawn_egg",
            EEEntities.PROPELLER.get(),
            3640234, 6052694);

    public static void register(RegisterFunction<Item> function) {
        items.forEach(((id, item) -> function.register(BuiltInRegistries.ITEM, EEExpanded.id(id), item.get())));
    }

    /**
     * Creates and saves a spawn egg item for the given entity type, util method that calls the generic item method
     * with default model and tab addition set to true.
     *
     * @param name           The registry name of the item used in registration
     * @param entityType     The entity type this spawn egg will spawn
     * @param primaryColor   The primary color of the spawn egg
     * @param secondaryColor The secondary color of the spawn egg
     * @return The deferred object that holds the spawn egg item instance
     */
    static DeferredObject<Item> spawnEgg(String name, EntityType<? extends Mob> entityType, int primaryColor,
                                         int secondaryColor) {
        DeferredObject<Item> ret = item(name,
                () -> new SpawnEggItem(entityType, primaryColor, secondaryColor, new Item.Properties()), false, false);
        EEECreativeTabs.addSpawnEgg(ret);
        return ret;
    }

    /**
     * Save the item for registration on the different loaders, also giving the option to add the item to the mod's
     * creative
     * tab and data gen the model in case is a simple item (item with just a png as texture/model)
     *
     * @param name         The registry name of the item used in registration
     * @param itemSupplier The supplier that provides the item instance when requested
     * @param defaultModel Whether to data gen a simple item model for this item
     * @param defaultTab   Whether to add this item to the mod's creative tab by default
     * @return The deferred object that holds the item instance
     */
    static <T extends Item> DeferredObject<T> item(String name, Supplier<T> itemSupplier, boolean defaultModel,
                                                   boolean defaultTab) {
        var ret = new DeferredObject<>(itemSupplier);
        items.put(name, ret);
        if (defaultTab) EEECreativeTabs.addItem(ret);
        if (defaultModel) EEEDataGenProcessor.addDefaultItem(name, ret);
        return ret;
    }
}
