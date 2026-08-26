package com.juiceybeans.eeexpanded.entity;

import com.juiceybeans.eeexpanded.init.EEESoundEvents;
import net.minecraft.core.BlockPos;
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
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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

public class DragonflyEntity extends Monster implements GeoEntity, NeutralMob {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> HURT_STAGE =
            SynchedEntityData.defineId(DragonflyEntity.class, EntityDataSerializers.INT);

    private long hurtStageStart = 0;

    private static final long FIRST_DODGE_DELAY = 30;
    private static float RANDOM_DODGE_DELAY = 0;

    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersisterAngerTime;
    private @Nullable UUID persistentAngerTarget;

    public DragonflyEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 15;
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 3.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.FLYING_SPEED, 0.32D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HURT_STAGE, 0);
    }

    private int getHurtStage() {
        return this.entityData.get(HURT_STAGE);
    }

    private void setHurtStage(int stage) {
        this.entityData.set(HURT_STAGE, stage);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D) {
            @Override
            protected @Nullable Vec3 getPosition() {
                RandomSource random = DragonflyEntity.this.getRandom();
                double x = DragonflyEntity.this.getX() + ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
                double y = DragonflyEntity.this.getY() + ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
                double z = DragonflyEntity.this.getZ() + ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
                return new Vec3(x, y, z);
            }
        });
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new FloatGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false, false) {
            @Override
            public boolean canUse() {
                return (super.canUse() && level().isNight());
            }
        });
    }

    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    @Override
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() + 0.5D;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isAngry() ? SoundEvents.BEE_LOOP_AGGRESSIVE : SoundEvents.BEE_LOOP;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(EEESoundEvents.SILENT_STEP, 0.15F, 1.0F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BEE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BEE_DEATH;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        entity.startRiding(this);
        this.setDeltaMovement(new Vec3(0.0, 0.2, 0.0));

        if (entity instanceof LivingEntity living) living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));

        return super.doHurtTarget(entity);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide()) return false;

        if (source.getEntity() instanceof Player player && player.isCreative()) return super.hurt(source, amount);
        if (source == this.damageSources().fall()) return false;
        if (source == this.damageSources().cactus()) return false;

        this.removeEffect(MobEffects.DIG_SPEED);
        if (source.getEntity() instanceof LivingEntity attacker) {
            this.setDeltaMovement(new Vec3(
                    Math.sin(Math.toRadians(attacker.getYRot() + 180.0F)) * 1.3D,
                    -0.2D,
                    Math.cos(Math.toRadians(attacker.getYRot() + 180.0F)) * 1.3D)
            );
        }

        setHurtStage(1);

        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;
        if (!this.isAlive()) return;

        long thisTick = this.level().getGameTime();

        if (getRandom().nextDouble() < 0.01) {
            this.setDeltaMovement(new Vec3(
                    Math.sin(Math.toRadians(this.getYRot() + Mth.nextDouble(getRandom(), 90.0, 270.0))) * 0.7D,
                    0.0D,
                    Math.cos(Math.toRadians(this.getYRot())) * 0.7D)
            );
        }

        if (getHurtStage() == 1 && thisTick >= hurtStageStart + FIRST_DODGE_DELAY) {
            if (this.hasEffect(MobEffects.DIG_SPEED)) {
                this.setDeltaMovement(new Vec3(
                        Math.sin(Math.toRadians((this.getYRot() + 180.0F))) * 0.7D,
                        -0.3D,
                        Math.cos(Math.toRadians(this.getYRot())) * 0.7D)
                );

                RANDOM_DODGE_DELAY = Mth.randomBetween(getRandom(), 20.0f, 300.0f);
                setHurtStage(2);
                hurtStageStart = thisTick;
            }
        }

        if (getHurtStage() == 2 && thisTick >= hurtStageStart + RANDOM_DODGE_DELAY) {
            for (int i = 0; i < 10; i++) {
                this.setDeltaMovement(new Vec3(
                        Math.sin(Math.toRadians(this.getYRot() +
                                Mth.randomBetween(this.getRandom(), 90.0f, 270.0f))) * 0.4D,
                        0.0D,
                        Math.cos(Math.toRadians(this.getYRot())) * 0.4D)
                );
            }

            RANDOM_DODGE_DELAY = Mth.randomBetween(getRandom(), 30.0f, 300.0f);
            setHurtStage(3);
            hurtStageStart = thisTick;
        }

        if (getHurtStage() == 2 && thisTick >= hurtStageStart + RANDOM_DODGE_DELAY) {
            for (int i = 0; i < 10; i++) {
                if (Math.random() < 0.5D) {
                    this.setDeltaMovement(new Vec3(
                            Math.sin(Math.toRadians(this.getYRot() +
                                    Mth.randomBetween(this.getRandom(), 90.0f, 100.0f))) * 0.6D,
                            0.0D,
                            Math.cos(Math.toRadians(this.getYRot())) * 0.3D
                    ));
                } else {
                    this.setDeltaMovement(new Vec3(
                            Math.sin(Math.toRadians(this.getYRot() +
                                    Mth.randomBetween(this.getRandom(), 260.0f, 270.0f))) * 0.6D,
                            0.0D,
                            Math.cos(Math.toRadians(this.getYRot())) * 0.3D
                    ));
                }
            }

            RANDOM_DODGE_DELAY = 0;
            setHurtStage(0);
            hurtStageStart = 0;
        }
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
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
        controllers.add(new AnimationController<>(this, "Walk/Idle", 4,
                state -> state.setAndContinue(IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
