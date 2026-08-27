package com.juiceybeans.eeexpanded.init;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.core.DeferredObject;
import com.juiceybeans.eeexpanded.core.RegisterFunction;
import com.juiceybeans.eeexpanded.entity.*;
import com.juiceybeans.eeexpanded.entity.projectile.GallantSwingsEntity;
import com.juiceybeans.eeexpanded.mixin.SpawnPlacementsAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class EEEntities {

    static Map<String, DeferredObject<EntityType<? extends Entity>>> entityTypes = new HashMap<>();

    public static final DeferredObject<EntityType<GallantEntity>> GALLANT = entity("gallant",
            EntityType.Builder.of(GallantEntity::new, MobCategory.MONSTER)
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(0.8F, 1.95F));
    public static final DeferredObject<EntityType<CinderEntity>> CINDER = entity("cinder",
            EntityType.Builder.of(CinderEntity::new, MobCategory.MONSTER)
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(0.6F, 1.8F));
    public static final DeferredObject<EntityType<EyestalkerEntity>> EYESTALKER = entity("eyestalker",
            EntityType.Builder.of(EyestalkerEntity::new, MobCategory.MONSTER)
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(1.2F, 2.95F));
    public static final DeferredObject<EntityType<DragonflyEntity>> DRAGONFLY = entity("dragonfly",
            EntityType.Builder.of(DragonflyEntity::new, MobCategory.MONSTER)
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(0.8F, 1.95F));
    public static final DeferredObject<EntityType<GuardsmanEntity>> GUARDSMAN = entity("guardsman",
            EntityType.Builder.of(GuardsmanEntity::new, MobCategory.MONSTER)
                    .setShouldReceiveVelocityUpdates(true)
                    .sized(0.7F, 2.45F));

    // projectiles
    public static final DeferredObject<EntityType<GallantSwingsEntity>> GALLANT_SWINGS = entity("gallant_swings",
            EntityType.Builder.<GallantSwingsEntity>of(GallantSwingsEntity::new, MobCategory.MISC)
                    .setShouldReceiveVelocityUpdates(true)
                    .setUpdateInterval(1)
                    .sized(0.5F, 0.5F));

    private EEEntities() {}

    // Registers
    public static void register(RegisterFunction<EntityType<?>> function) {
        entityTypes.forEach(((id, entityType) -> function.register(
                BuiltInRegistries.ENTITY_TYPE, EEExpanded.id(id), entityType.get())));
        EEEntities.registerSpawns();
    }

    public static void registerSpawns() {
        registerMobSpawn(EEEntities.GALLANT.get());
    }

    public static void registerMobSpawn(EntityType<?> type) {
        SpawnPlacementsAccessor.callRegister(type, SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EEEntities::checkHostileRules);
    }

    public static void registerAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> consumer) {
        consumer.accept(GALLANT.get(), GallantEntity.createAttributes().build());
        consumer.accept(CINDER.get(), CinderEntity.createAttributes().build());
        consumer.accept(EYESTALKER.get(), EyestalkerEntity.createAttributes().build());
        consumer.accept(DRAGONFLY.get(), DragonflyEntity.createAttributes().build());
        consumer.accept(GUARDSMAN.get(), GuardsmanEntity.createAttributes().build());
    }

    public static Map<String, DeferredObject<EntityType<? extends Entity>>> getEntityTypes() {
        return entityTypes;
    }

    static <T extends Entity> DeferredObject<EntityType<T>> entity(EntityType.EntityFactory<T> factory, String name,
                                                                   MobCategory category, float width, float height) {
        return entity(name, EntityType.Builder.of(factory, category).sized(width, height).clientTrackingRange(64)
                .updateInterval(3));
    }

    @SuppressWarnings("unchecked")
    static <T extends Entity> DeferredObject<EntityType<T>> entity(String name, EntityType.Builder<T> builder) {
        DeferredObject<EntityType<T>> ret = new DeferredObject<>(() -> builder.build(EEExpanded.id(name).toString()));
        entityTypes.put(name, (DeferredObject<EntityType<? extends Entity>>) (Object) ret);
        return ret;
    }

    /**
     * Common hostile mob spawn rules check used by most hostile mobs.
     */
    public static boolean checkHostileRules(EntityType<?> type, ServerLevelAccessor level, MobSpawnType spawnType,
                                            BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn(level, pos, random) &&
                Mob.checkMobSpawnRules((EntityType<? extends Mob>) type, level, spawnType, pos, random);
    }
}
