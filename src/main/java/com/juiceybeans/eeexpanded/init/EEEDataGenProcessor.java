package com.juiceybeans.eeexpanded.init;

import com.juiceybeans.eeexpanded.core.DeferredObject;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class EEEDataGenProcessor {
    static final Map<String, DeferredObject<? extends Item>> defaultItems = new HashMap<>();

    /**
     * Default item with just a png as a model and texture
     */
    public static void addDefaultItem(String name, DeferredObject<? extends Item> item) {
        defaultItems.put(name, item);
    }

    public static Map<String, DeferredObject<? extends Item>> getDefaultItems() {
        return defaultItems;
    }
}
