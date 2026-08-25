package com.juiceybeans.eeexpanded.entity;

import com.juiceybeans.eeexpanded.entity.projectile.GallantSwingsEntity;
import com.juiceybeans.eeexpanded.init.EEEntities;
import com.juiceybeans.eeexpanded.init.EESoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
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
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GallantEntity extends Monster implements GeoEntity {
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation SHIELD = RawAnimation.begin().thenPlay("shielding");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> HURT_STAGE =
            SynchedEntityData.defineId(GallantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_STAGE =
            SynchedEntityData.defineId(GallantEntity.class, EntityDataSerializers.INT);

    private long hurtStageStart = 0;
    private long attackStageStart = 0;

    private boolean isAttackerInWater;

    private static final long CHARGE_DELAY = 16;
    private static final long SWING_DELAY = 6;
    private static final long FIRST_WAVE_DELAY = 12;
    private static final long SECOND_WAVE_DELAY = 8;

    public GallantEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 12;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 3.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
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
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, false, false) {
            @Override
            public boolean canUse() {
                Level world = GallantEntity.this.level();
                return super.canUse() && world.isNight();
            }
        });
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Villager.class, false, false));
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() + 0.5D;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return EESoundEvents.SILENT_STEP;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 0.1F, 1.0F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
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
        if (source == this.damageSources().lava()) return false;
        if (source == this.damageSources().drown()) return false;
        if (source == this.damageSources().fall()) return false;
        if (source == this.damageSources().cactus()) return false;
        if (source == this.damageSources().wither()) return false;
        if (source.type() == this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(DamageTypes.WITHER_SKULL)) return false; // if there's a better way to do this LET ME KNOW!!

        // raise shield
        if (getAttackStage() == 0 && getHurtStage() == 0
                && (source.getEntity() instanceof Player || source.getEntity() instanceof Arrow)
                && level().random.nextInt() <= 0.75D) {

            if (source.getEntity() instanceof Player player && player.isCreative()) return false;

            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 2, false, false));
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 14, 1, false, false));

            this.level().playSound(null, this.blockPosition(), SoundEvents.BLAZE_HURT, SoundSource.HOSTILE, 1.0F, 0.0F);

            setHurtStage(1);
            this.hurtStageStart = this.level().getGameTime();
            this.isAttackerInWater = source.getEntity().isInWater();
        }

        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;
        if (!this.isAlive()) return;

        if (this.isInWaterRainOrBubble()) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, false));
        }

        if (this.getHealth() < 12.0F) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false));
        }

        if (this.getHealth() < 6.0F) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false));
        }

        long thisTick = this.level().getGameTime();

        if (getHurtStage() == 1 && thisTick >= this.hurtStageStart + CHARGE_DELAY) { // charge
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 12, 2, false, false));
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 1, false, false));

            setHurtStage(2);
            this.hurtStageStart = thisTick;
        }

        if (getHurtStage() == 2 && thisTick >= this.hurtStageStart + SWING_DELAY) { // swing
            var damage = isAttackerInWater ? 8.0F : 14.0F;
            var speed = isAttackerInWater ? 0.6F : 1.0F;
            var spreadRange = isAttackerInWater ? 100.0F : 180.0F;

            swingBurstAttack(damage, spreadRange, speed);

            setHurtStage(0);
            this.hurtStageStart = 0;
            isAttackerInWater = false;
        }

        if (getAttackStage() == 1 && thisTick >= this.attackStageStart + FIRST_WAVE_DELAY) {
            swingBurstAttack(6.0F, 180.0F, 1.0F);
            this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.0F, 0.0F);

            setAttackStage(2);
            this.attackStageStart = thisTick;
        }

        if (getAttackStage() == 2 && thisTick >= this.attackStageStart + SECOND_WAVE_DELAY) {
            swingBurstAttack(6.0F, 180.0F, 1.0F);
            this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.0F, 0.0F);

            setAttackStage(0);
            this.attackStageStart = 0;
        }
    }

    private void swingBurstAttack(float damage, float spreadRange, float speed) {
        for (int i = 0; i < 10; i++) {
            GallantSwingsEntity projectile = new GallantSwingsEntity(EEEntities.GALLANT_SWINGS.get(), level());

            projectile.setBaseDamage(damage);
            projectile.setKnockback(1);
            projectile.setNoGravity(true);
            projectile.setPos(this.getX(), this.getEyeY() - 0.1D, this.getZ());

            float inaccuracy = 1.0F + level().random.nextFloat() * (spreadRange - 1.0F);

            projectile.shoot(
                    this.getLookAngle().x,
                    this.getLookAngle().y,
                    this.getLookAngle().z,
                    speed,
                    inaccuracy
            );
            level().addFreshEntity(projectile);
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.0F, 0.0F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Walk/Idle", 4, state -> {
            if (state.isMoving()) return state.setAndContinue(WALK);
            return state.setAndContinue(IDLE);
        }));

        controllers.add(new AnimationController<>(this, "Attack", 0, state -> {
            if (getAttackStage() >= 1) {
                return state.setAndContinue(ATTACK);
            }

            if (getHurtStage() >= 1) {
                return state.setAndContinue(SHIELD);
            }

            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
