package com.juiceybeans.eeexpanded.entity;

import com.juiceybeans.eeexpanded.EEExpanded;
import com.juiceybeans.eeexpanded.init.EEEMobEffects;
import com.juiceybeans.eeexpanded.init.EEESoundEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
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

public class KelpieEntity extends PathfinderMob implements GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public KelpieEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        xpReward = 15;
        setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        moveControl = new MoveControl(this) {

            @Override
            public void tick() { // todo SEE IF THIS WORKS!! THIS IS JUST 1:1 PORTED!!
                if (isInWater()) {
                    setDeltaMovement(getDeltaMovement().add(0.0, 0.005, 0.0));
                }

                if (operation == Operation.MOVE_TO && !getNavigation().isDone()) {
                    double dx = this.wantedX - getX();
                    double dy = this.wantedY - getY();
                    double dz = this.wantedZ - getZ();

                    float f = (float) ((Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F);
                    float f1 = (float) (this.speedModifier * getAttribute(Attributes.MOVEMENT_SPEED).getValue());

                    setYRot(this.rotlerp(getYRot(), f, 10.0F));
                    yBodyRot = getYRot();
                    yHeadRot = getYHeadRot();

                    if (isInWater()) {
                        setSpeed((float) getAttribute(Attributes.MOVEMENT_SPEED).getValue());

                        float f2 = -((float) (Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180.0 / Math.PI)));
                        f2 = Mth.clamp(Mth.wrapDegrees(f2), -85.0F, 85.0F);
                        setXRot(this.rotlerp(getXRot(), f2, 5.0F));

                        float f3 = Mth.cos((float) (getXRot() * (Math.PI / 180.0)));
                        setZza(f3 * f1);
                        setYya((float) (f1 * dy));
                    } else {
                        setSpeed(f1 * 0.05F);
                    }
                } else {
                    setSpeed(0);
                    setYya(0);
                    setZza(0);
                }
            }
        };
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(ForgeMod.SWIM_SPEED.get(), 0.3D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(2, new RandomSwimmingGoal(this, 1.0, 40));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    @Override
    public MobType getMobType() {
        return MobType.WATER;
    }

    @Override
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() + -0.4D;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return EEESoundEvents.KELPIE_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.DROWNED_HURT_WATER;
    } // todo check if adding a land or water check is needed

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.DROWNED_DEATH_WATER;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide()) return false;
        if (!isAlive()) return false;

        removeAllEffects();
        addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 0));

        Entity sourceEntity = source.getEntity();

        if (sourceEntity instanceof LivingEntity) {
            if (sourceEntity instanceof Player player) {
                setDeltaMovement(new Vec3(player.getLookAngle().x * 2, -0.5, player.getLookAngle().z * 2));
                EEExpanded.scheduleTask((ServerLevel) level(), 20, () -> {
                    var dx = 4.0;
                    if (getRandom().nextDouble() < 0.5) dx = -4.0;
                    setDeltaMovement(
                            new Vec3(dx, Mth.nextDouble(getRandom(), 0.0, 1.0),
                                    Mth.nextDouble(getRandom(), -2.0, 2.0)));

                    for (Drowned drowned : level().getEntitiesOfClass(Drowned.class,
                            new AABB(position(), position()).inflate(32))) {
                        drowned.addEffect(
                                new MobEffectInstance(MobEffects.CONDUIT_POWER, Mth.nextInt(getRandom(), 150, 250), 0,
                                        false, true));
                        drowned.heal(4);
                    }
                });
            }

            if (level().getBlockState(new BlockPos(
                    (int) (sourceEntity.getX() - Mth.nextDouble(getRandom(), -8.0, 8.0)),
                    (int) (sourceEntity.getY() - 1.0),
                    (int) (sourceEntity.getZ() - Mth.nextDouble(getRandom(), -8.0, 8.0)))).is(Blocks.WATER)) {
                level().playSound(null, sourceEntity.getX(), sourceEntity.getY(), sourceEntity.getZ(),
                        EEESoundEvents.KELPIE_REINFORCEMENT, SoundSource.HOSTILE, 1.0F, 1.0F);

                EEExpanded.scheduleTask((ServerLevel) level(), 30, () -> {
                    if (getTarget() == null) return;

                    for (int i = 0; i < Mth.nextInt(getRandom(), 1, 2); i++) {
                        Drowned drowned = new Drowned(EntityType.DROWNED, level());
                        drowned.moveTo(
                                (int) (getTarget().getX() - Mth.nextDouble(getRandom(), -8.0, 8.0)),
                                (int) (getTarget().getY() - 1.0),
                                (int) (getTarget().getZ() - Mth.nextDouble(getRandom(), -8.0, 8.0)),
                                getRandom().nextFloat() * 360.0F, 0.0F);
                        drowned.setHealth(10);
                        drowned.addEffect(
                                new MobEffectInstance(MobEffects.CONDUIT_POWER, Mth.nextInt(getRandom(), 150, 250), 0,
                                        false, true));
                        drowned.finalizeSpawn((ServerLevelAccessor) level(),
                                level().getCurrentDifficultyAt(drowned.blockPosition()), MobSpawnType.MOB_SUMMONED,
                                null, null);
                        level().addFreshEntity(drowned);

                        level().playSound(null, drowned.getX(), drowned.getY(), drowned.getZ(),
                                SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.HOSTILE, 1.0F, 1.0F);
                    }
                });
            }

            ((ServerLevel) level()).sendParticles(ParticleTypes.SOUL, getX(), getY(), getZ(), 30, 3.0, 3.0, 3.0, 0.3);

            if (!sourceEntity.isInWater()) setDeltaMovement(new Vec3(getLookAngle().x * 2, 0.5, getLookAngle().z * 2));
        }

        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource damageSource) {
        this.ejectPassengers();

        super.die(damageSource);

        for (Drowned drowned : level().getEntitiesOfClass(Drowned.class,
                new AABB(position(), position()).inflate(36))) {
            drowned.kill();
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (isAlive() && !level().isClientSide() && target != null) {
            if (!hasEffect(MobEffects.DOLPHINS_GRACE) && !hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 162, 0, false, false));

                EEExpanded.scheduleTask((ServerLevel) level(), 6, () -> {
                    level().playSound(null, getX(), getY(), getZ(),
                            SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.0F, 1.0F);
                    // todo this was originally a timer so remake it
                    addEffect(new MobEffectInstance(MobEffects.LUCK, 15, 0, false, false));
                });
            }

            if (hasEffect(MobEffects.LUCK)) {
                Vec3 center = new Vec3(getX(), getY() + 1.0, target.getZ() - 1.0);
                var hasRider = false;
                for (Player player : level().getEntitiesOfClass(Player.class, new AABB(center, center).inflate(3.0))) {
                    if (!hasRider) {
                        triggerAnim("Attack", "attack");
                        hasRider = true;

                        // delay for animation
                        EEExpanded.scheduleTask((ServerLevel) level(), 20, () -> {
                            player.startRiding(this);
                            EEExpanded.scheduleTask((ServerLevel) level(), 4 * 20, () -> {
                                if (player.getVehicle() == this) player.stopRiding();
                            });
                        });
                    }

                    this.addEffect(new MobEffectInstance(EEEMobEffects.GROUND_BOUND.get(), 20, 2, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20, 0, false, false));
                }
            }
        }

        super.setTarget(target);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                                  @Nullable CompoundTag dataTag) {
        addEffect(new MobEffectInstance(EEEMobEffects.GROUND_BOUND.get(), 20, 9, false, false));
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        if (!isAlive()) return;

        if (!isInWater() && onGround()) {
            setDeltaMovement(new Vec3(getLookAngle().x, Mth.nextDouble(getRandom(), 0.2, 0.5), getLookAngle().z));
        }
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        return level.isUnobstructed(this);
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Walk/Idle", 4, state -> {
            if (state.isMoving()) return state.setAndContinue(WALK);
            return state.setAndContinue(IDLE);
        }));

        controllers.add(new AnimationController<>(this, "Attack", 4, state -> PlayState.CONTINUE)
                .triggerableAnim("attack", ATTACK));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
