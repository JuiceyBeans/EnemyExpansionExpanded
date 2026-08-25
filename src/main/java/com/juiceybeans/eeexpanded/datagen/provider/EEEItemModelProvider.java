package com.juiceybeans.eeexpanded.datagen.provider;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.core.DeferredObject;
import com.juiceybeans.eeexpanded.init.EEECreativeTabs;
import com.juiceybeans.eeexpanded.init.EEEDataGenProcessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class EEEItemModelProvider extends ItemModelProvider {
    public EEEItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, EEExpanded.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        EEEDataGenProcessor.getDefaultItems().forEach((name, item) -> simpleItem(name));

        EEECreativeTabs.getSpawnEggs().forEach(item -> spawnEgg((DeferredObject<Item>) item));
    }

    private void spawnEgg(DeferredObject<Item> item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item.get());
        withExistingParent(id.getPath(), mcLoc("item/template_spawn_egg"));
    }

    private void simpleItem(String name) {
        withExistingParent(name,
                ResourceLocation.withDefaultNamespace("item/generated")).texture(
                "layer0",
                EEExpanded.id("item/" + name)
        );
    }
}