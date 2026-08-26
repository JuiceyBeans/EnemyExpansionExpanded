package com.juiceybeans.eeexpanded.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class EyestalkerEntity extends Monster implements GeoEntity, NeutralMob {

    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> HURT_STAGE = SynchedEntityData.defineId(EyestalkerEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_STAGE = SynchedEntityData.defineId(EyestalkerEntity.class,
            EntityDataSerializers.INT);

    private long hurtStageStart = 0;
    private long attackStageStart = 0;

    private static final long SPIKE_DELAY = 30;
    private static final long ATTACK_DELAY = 15; // animation reset time

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersisterAngerTime;
    private @Nullable UUID persistentAngerTarget;

    public EyestalkerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.42D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HURT_STAGE, 0);
        this.entityData.define(ATTACK_STAGE, 0);
    }

    private int getHurtStage() {
        return this.entityData.get(HURT_STAGE);
    }

    private void setHurtStage(int stage) {
        this.entityData.set(HURT_STAGE, stage);
    }

    private int getAttackStage() {
        return this.entityData.get(ATTACK_STAGE);
    }

    private void setAttackStage(int stage) {
        this.entityData.set(ATTACK_STAGE, stage);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        entity.setSecondsOnFire(5);

        if (getAttackStage() == 0) {
            setAttackStage(1);
            this.attackStageStart = this.level().getGameTime();
            return true;
        } else return false;
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);

        if (!level().isClientSide && !player.isCreative()) {
            this.setPersistentAngerTarget(player.getUUID());
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide()) return false;

        if (source == this.damageSources().fall()) return false;

        if (source.getEntity() instanceof Player player) {
            setHurtStage(1);
            this.hurtStageStart = this.level().getGameTime();

            double velX = 2.0 * Math.sin(Math.toRadians(player.getYRot()));
            double velZ = 2.0 * Math.cos(Math.toRadians(player.getYRot()));
            this.setDeltaMovement(new Vec3(player.getLookAngle().x * velX, -0.3, player.getLookAngle().z * velZ));

            this.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 4, false, true));
            this.playSound(SoundEvents.ENDER_EYE_LAUNCH);
        }

        return super.hurt(source, amount);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(1.5F);
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;
        if (!this.isAlive()) return;

        if (getHurtStage() == 1 && this.level().getGameTime() >= hurtStageStart + SPIKE_DELAY) {
            if (this.getLastAttacker() != null) {
                Vec3 lookVector = getLastAttacker().position().subtract(this.position());

                this.setYRot((float) (Mth.atan2(lookVector.z, lookVector.x) * (180F / Math.PI)) - 90F);
                this.setXRot(0.0F);
                this.playSound(SoundEvents.ENDER_EYE_DEATH);

                Vec3 forwardVector = lookVector.normalize();

                double xMult = Mth.nextDouble(this.getRandom(), 1.25, 2.0);
                double yMult = Mth.nextDouble(this.getRandom(), 0.3, 0.6);
                double zMult = Mth.nextDouble(this.getRandom(), 1.25, 2.0);

                this.setDeltaMovement(forwardVector.x * xMult, 0.3 * yMult, forwardVector.z * zMult);
            }

            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, false, true));

            setHurtStage(0);
            this.hurtStageStart = 0;
        }

        // reset attack anim
        if (getAttackStage() == 1 && this.level().getGameTime() >= attackStageStart + ATTACK_DELAY) {
            setAttackStage(0);
            this.attackStageStart = 0;
        }
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel) this.level(), true);
        }
        super.aiStep();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        addPersistentAngerSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        readPersistentAngerSaveData(this.level(), compound);
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersisterAngerTime;
    }

    @Override
    public void setRemainingPersistentAngerTime(int remainingPersistentAngerTime) {
        this.remainingPersisterAngerTime = remainingPersistentAngerTime;
    }

    @Override
    public @Nullable UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID persistentAngerTarget) {
        this.persistentAngerTarget = persistentAngerTarget;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Walk/Idle", 4, state -> {
            if (state.isMoving()) return state.setAndContinue(WALK);
            return state.setAndContinue(IDLE);
        }));

        controllers.add(new AnimationController<>(this, "Attack", 0, state -> {
            if (getAttackStage() >= 1) return state.setAndContinue(ATTACK);
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
