package com.juiceybeans.eeexpanded.datagen;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.datagen.provider.EEEItemModelProvider;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EEExpanded.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EEEDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new EEEItemModelProvider(packOutput, existingFileHelper));
    }
}
