package com.juiceybeans.eeexpanded.entity;

import com.juiceybeans.eeexpanded.EEExpanded;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.network.NetworkHooks;

import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PropellerEntity extends AbstractSkeleton implements GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation HURT = RawAnimation.begin().thenPlay("hurt");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> WEATHERED = SynchedEntityData.defineId(PropellerEntity.class,
            EntityDataSerializers.BOOLEAN);

    public PropellerEntity(EntityType<? extends AbstractSkeleton> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        xpReward = 6;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(WEATHERED, false);
    }

    public boolean getWeathered() {
        return this.entityData.get(WEATHERED);
    }

    private void setWeathered(boolean flag) {
        this.entityData.set(WEATHERED, flag);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 6.0F, 1.2, 1.4));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0F));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.COPPER_PLACE;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.LANTERN_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.UI_STONECUTTER_TAKE_RESULT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ITEM_BREAK;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!level().isClientSide) {
            ((ServerLevel) level()).sendParticles(
                    ParticleTypes.END_ROD,
                    this.getX(),
                    this.getEyeY(),
                    this.getZ(),
                    10,
                    0.6, 0.6, 0.6, 0.0);
        }

        super.performRangedAttack(target, distanceFactor);
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {
        removeAllEffects();

        addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 0, true, false));
        addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 2, false, false));

        setWeathered(false);

        super.thunderHit(level, lightning);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide()) return false;

        if (source == this.damageSources().fall()) return false;
        if (source == this.damageSources().lightningBolt()) return false;

        if (source.getDirectEntity() instanceof LivingEntity) {
            triggerAnim("Hurt", "hurt");

            if (isDeadOrDying()) {
                if (level().getBlockState(new BlockPos(this.getBlockX(), this.getBlockY() + 1, this.getBlockZ()))
                        .is(Blocks.AIR)) {
                    setDeltaMovement(0.0, 0.2, 0.0);

                    EEExpanded.scheduleTask((ServerLevel) level(), 2, () -> {
                        setDeltaMovement(0.0, 1.2, 0.0);
                        addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, false));

                        EEExpanded.scheduleTask((ServerLevel) level(), 4, () -> setDeltaMovement(
                                Mth.nextDouble(getRandom(), -1.0, 1.5),
                                Mth.nextDouble(getRandom(), 0.3, 2.0),
                                Mth.nextDouble(getRandom(), -1.0, 1.5)));
                    });

                    if (getWeathered() && getRandom().nextDouble() < 0.25 && getHealth() <= 8.0F) {
                        setWeathered(true);
                        addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, 2, false, false));
                    }
                }

                if (source.getEntity() instanceof Arrow) {
                    kill();
                    ((ServerLevel) level()).sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 30, 0.6, 0.6,
                            0.6,
                            0.0);
                    // todo grant precision strike advancement
                }
            }
        }

        return super.hurt(source, amount);
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        if (isOnFire()) {
            var lootTable = ((ServerLevel) level()).getServer().getLootData()
                    .getLootTable(EEExpanded.id("entities/propeller_oneshot"));
            var params = new LootParams.Builder((ServerLevel) level())
                    .withParameter(LootContextParams.ORIGIN, this.position())
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                    .create(LootContextParamSets.ENTITY);

            var items = lootTable.getRandomItems(params);

            for (ItemStack item : items) {
                spawnAtLocation(item);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!isAlive() || level().isClientSide()) return;

        if (!getWeathered() && isInWaterOrRain() && !hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            setWeathered(true);
            addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, false));
            ((ServerLevel) level()).sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 15, 0.3, 0.3, 0.3,
                    0.3);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Walk/Idle", 4, state -> {
            if (state.isMoving()) return state.setAndContinue(WALK);
            return state.setAndContinue(IDLE);
        }));

        controllers.add(new AnimationController<>(this, "Hurt", 4, state -> PlayState.STOP)
                .triggerableAnim("hurt", HURT));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
