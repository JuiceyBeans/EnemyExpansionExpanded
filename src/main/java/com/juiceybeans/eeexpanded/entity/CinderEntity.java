package com.juiceybeans.eeexpanded.entity;

import com.juiceybeans.eeexpanded.entity.projectile.CinderFireballEntity;
import com.juiceybeans.eeexpanded.init.EEEntities;
import com.juiceybeans.eeexpanded.init.EEESoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CinderEntity extends Monster implements GeoEntity {
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> HURT_STAGE =
            SynchedEntityData.defineId(CinderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_STAGE =
            SynchedEntityData.defineId(CinderEntity.class, EntityDataSerializers.INT);

    private long hurtStageStart = 0;
    private long attackStageStart = 0;

    private static final long FIREBALL_DELAY = 60;
    private static final long ATTACK_DELAY = 10;

    public CinderEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
        this.xpReward = 15;
        this.setPersistenceRequired();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D);
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
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this).setAlertOthers());
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(EEESoundEvents.SILENT_STEP, 0.15F, 1.0F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (getAttackStage() == 0) {
            setAttackStage(1);
            this.attackStageStart = this.level().getGameTime();
            return true;
        } else return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide()) return false;

        if (source.getEntity() instanceof Player player && player.isCreative()) return super.hurt(source, amount);
        if (source.getEntity() instanceof CinderFireballEntity) return false;
        if (source.getEntity() instanceof AbstractArrow) return false;
        if (source == this.damageSources().fall()) return false;
        if (source == this.damageSources().lava()) return false;
        if (source == this.damageSources().drown()) return false;
        if (source == this.damageSources().fall()) return false;
        if (source == this.damageSources().cactus()) return false;
        if (source == this.damageSources().wither()) return false;
        if (source.type() == this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(DamageTypes.WITHER_SKULL)) return false; // if there's a better way to do this LET ME KNOW!!
        if (source.type() == this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(DamageTypes.FALLING_ANVIL)) return false;

        if (source.getEntity() instanceof Player) {
            setHurtStage(1);

            if (level().random.nextFloat() < 0.5f) {
                this.setSecondsOnFire(3);
                this.playSound(SoundEvents.FIRECHARGE_USE);

                hurtStageStart = this.level().getGameTime(); // we summon a FIREBALL in 3 seconds
            }

            if (level().random.nextFloat() < 0.3f && level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).is(Blocks.AIR)) {
                this.setDeltaMovement(new Vec3(-0.5D, 1.0D, -0.5D));
            } else {
                this.setDeltaMovement(new Vec3(0.5D, 1.0D, 0.5D));
            }
        }

        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;
        if (!this.isAlive()) return;

        if (this.isInWaterRainOrBubble()) {
            level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 3.0D, 3.0D, 1.0D);
        }

        this.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, false, false));
        this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false));

        if (level().getBlockState(new BlockPos(this.getBlockX(), this.getBlockY() - 1, this.getBlockZ())).is(Blocks.LAVA)) {
            if (getRandom().nextInt() > 0.5D) this.setDeltaMovement(new Vec3(-0.6D, 3.0D, -0.6D));
            else this.setDeltaMovement(new Vec3(0.6D, 3.0D, 0.6D));
        }

        if (getHurtStage() == 1 && this.level().getGameTime() >= hurtStageStart + FIREBALL_DELAY) {
            CinderFireballEntity projectile = new CinderFireballEntity(EEEntities.CINDER_FIREBALL.get(), level());

            projectile.setBaseDamage(5.0f);
            projectile.setKnockback(1);
            projectile.setNoGravity(true);
            projectile.setPos(this.getX(), this.getEyeY() - 0.1D, this.getZ());
            projectile.setOwner(this);

            projectile.shoot(this.getLookAngle().x, this.getLookAngle().y, this.getLookAngle().z, 1.0f, 1.0f);
            level().addFreshEntity(projectile);
            this.playSound(SoundEvents.FIRECHARGE_USE);

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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Walk/Idle", 4,
                state -> state.setAndContinue(IDLE)));

        controllers.add(new AnimationController<>(this, "Attack", 0, state -> {
            if (getAttackStage() >= 1 || getHurtStage() == 1) return state.setAndContinue(ATTACK);
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
