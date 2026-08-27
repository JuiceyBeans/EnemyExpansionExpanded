package com.juiceybeans.eeexpanded.entity;

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
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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

import java.util.List;

public class GuardsmanEntity extends AbstractSkeleton implements GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation DODGE = RawAnimation.begin().thenPlay("dodge");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> RETALIATION = SynchedEntityData.defineId(GuardsmanEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEALING = SynchedEntityData.defineId(GuardsmanEntity.class,
            EntityDataSerializers.BOOLEAN);

    private long retaliationStart = 0;
    private long healingStart = 0;

    private static final long MELEE_DIST = 4;

    private static final long RETALIATION_DELAY = 20;
    private static final long HEALING_DELAY = 20;

    public GuardsmanEntity(EntityType<? extends AbstractSkeleton> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 8;
        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 28.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.37D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(RETALIATION, false);
        this.entityData.define(HEALING, false);
    }

    private boolean getRetaliation() {
        return this.entityData.get(RETALIATION);
    }

    private void setRetaliation(boolean flag) {
        this.entityData.set(RETALIATION, flag);
    }

    private boolean isHealing() {
        return this.entityData.get(HEALING);
    }

    private void setHealing(boolean flag) {
        this.entityData.set(HEALING, flag);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false) {

            @Override
            public boolean canUse() {
                if (getTarget() == null || !getTarget().isAlive()) return false;
                if (getTarget().hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) return false;
                if ((!isInWater() && distanceTo(getTarget()) > MELEE_DIST) ||
                        (isInWater() && distanceTo(getTarget()) > (double) MELEE_DIST / 2))
                    return false;
                return super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 6.0F, 1.0, 1.2) {

            @Override
            public boolean canUse() {
                return super.canUse() && getRandom().nextDouble() < 0.03;
            }
        });
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
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
    public boolean doHurtTarget(Entity entity) {
        if (!super.doHurtTarget(entity)) return false;
        if (level().isClientSide()) return false;

        double radius = 2.0D;
        AABB area = entity.getBoundingBox().inflate(radius);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                LivingEntity.class,
                area,
                e -> e != this && e.isAlive() && !e.isAlliedTo(this));

        // aoe
        for (LivingEntity victim : nearby) {
            var kb = this.getAttribute(Attributes.ATTACK_KNOCKBACK);
            victim.knockback(
                    kb == null ? 1.5f : (float) kb.getValue(),
                    (victim.getX() - this.getX()) / this.distanceTo(victim),
                    (victim.getZ() - this.getZ()) / this.distanceTo(victim));

            if (victim == entity) continue;
            if (!this.hasLineOfSight(victim)) continue;

            var attackDamage = this.getAttribute(Attributes.ATTACK_DAMAGE);
            victim.hurt(this.damageSources().mobAttack(this),
                    attackDamage == null ? 4.0f : (float) attackDamage.getValue());
        }

        level().playSound(null, BlockPos.containing(this.position()), SoundEvents.PLAYER_ATTACK_CRIT,
                SoundSource.HOSTILE, 1.0F, 1.0F);

        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide()) return false;
        if (!isAlive()) return false;

        if (source.getEntity() == this) return false;

        var doHurt = true;
        // dodge player and self arrows, but not other guardsmen
        if (source.getDirectEntity() instanceof AbstractArrow arrow &&
                !(source.getEntity() instanceof GuardsmanEntity)) {
            arrow.discard();

            ((ServerLevel) level()).sendParticles(
                    ParticleTypes.LARGE_SMOKE,
                    this.getX(),
                    this.getEyeY(),
                    this.getZ(),
                    10,
                    0.6, 0.6, 0.6, 0.0);

            setHealing(true);
            healingStart = level().getGameTime();

            doHurt = false;
        }

        if (!(source.getEntity() instanceof Player player && player.isCreative())) {
            setRetaliation(true);
            retaliationStart = level().getGameTime();
        }

        if (!isDeadOrDying()) triggerAnim("Attack", "dodge");

        return doHurt && super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        if (!this.isAlive()) return;

        if (getRetaliation() && level().getGameTime() >= retaliationStart + RETALIATION_DELAY) {
            level().playSound(null, BlockPos.containing(this.position()), SoundEvents.CROSSBOW_SHOOT,
                    SoundSource.HOSTILE, 3.0F, 1.0F);

            if (getTarget() != null) {
                for (int i = 0; i < 10; i++) {
                    Arrow arrow = new Arrow(level(), this);
                    arrow.setPos(getX(), getEyeY() - 0.1, getZ());
                    arrow.shoot(
                            getTarget().getX() - this.getX(), getTarget().getEyeY() - this.getEyeY(),
                            getTarget().getZ() - this.getZ(),
                            Mth.randomBetween(getRandom(), 1.0f, 1.6f),
                            Mth.randomBetween(getRandom(), 1.0f, 50.0f));
                    level().addFreshEntity(arrow);

                }
            }

            setRetaliation(false);
            retaliationStart = 0;
        }

        if (isHealing() && level().getGameTime() >= healingStart + HEALING_DELAY) {
            this.heal(2.0f);

            setHealing(false);
            healingStart = 0;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Attack", 4, state -> PlayState.CONTINUE)
                .triggerableAnim("dodge", DODGE));

        controllers.add(new AnimationController<>(this, "Run/Idle", 4, state -> {
            if (state.isMoving() || this.getTarget() != null) return state.setAndContinue(RUN);
            return state.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
