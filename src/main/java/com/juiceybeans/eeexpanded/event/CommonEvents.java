package com.juiceybeans.eeexpanded.event;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.core.RegisterFunction;
import com.juiceybeans.eeexpanded.init.EEECreativeTabs;
import com.juiceybeans.eeexpanded.init.EEEItems;
import com.juiceybeans.eeexpanded.init.EEEMobEffects;
import com.juiceybeans.eeexpanded.init.EEEntities;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.Consumer;

public class CommonEvents {

    @Mod.EventBusSubscriber(modid = EEExpanded.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void registerContent(RegisterEvent event) {
            if (event.getRegistryKey() == Registries.ENTITY_TYPE)
                register(event, EEEntities::register);
            else if (event.getRegistryKey() == Registries.ITEM)
                register(event, EEEItems::register);
            else if (event.getRegistryKey() == Registries.MOB_EFFECT)
                register(event, EEEMobEffects::register);
        }

        @SubscribeEvent
        public static void createEntityAttributes(EntityAttributeCreationEvent event) {
            EEEntities.registerAttributes(event::put);
        }

        @SubscribeEvent
        public static void creativeTabModifications(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
                EEECreativeTabs.getSpawnEggs().forEach(item -> event.accept(item.get()));
            }
        }

        @SubscribeEvent
        public static void addMobsToDungeonSpawners(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                DungeonHooks.addDungeonMob(EEEntities.GALLANT.get(), 120);
                DungeonHooks.addDungeonMob(EEEntities.GUARDSMAN.get(), 180);
            });
        }

        private static <T> void register(RegisterEvent event, Consumer<RegisterFunction<T>> consumer) {
            consumer.accept((registry, id, value) -> event.register(registry.key(), id, () -> value));
        }
    }

    @Mod.EventBusSubscriber(modid = EEExpanded.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {

        @SubscribeEvent
        public static void createCinderSpawns(MobSpawnEvent.FinalizeSpawn event) {
            if (!(event.getSpawnType() == MobSpawnType.NATURAL)) return;
            if (event.getEntity().getType() == EntityType.BLAZE) {
                if (event.getLevel().getRandom().nextFloat() >= 0.8) {
                    event.setSpawnCancelled(true);

                    var cinder = EEEntities.CINDER.get().create(event.getLevel().getLevel());
                    if (cinder != null) {
                        cinder.setPos(event.getEntity().position());
                        event.getLevel().addFreshEntity(cinder);
                    }
                }
            }
        }
    }
}
