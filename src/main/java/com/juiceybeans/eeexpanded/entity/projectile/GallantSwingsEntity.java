package com.juiceybeans.eeexpanded.entity.projectile;

import com.juiceybeans.eeexpanded.init.EEEItems;
import com.juiceybeans.eeexpanded.init.EEEntities;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;

public class GallantSwingsEntity extends AbstractArrow implements ItemSupplier {

    private int lifeTicks = 12;

    public GallantSwingsEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public GallantSwingsEntity(EntityType<? extends AbstractArrow> entityType, double x, double y, double z,
                               Level level) {
        super(entityType, x, y, z, level);
        this.setNoGravity(true);
    }

    public GallantSwingsEntity(EntityType<? extends AbstractArrow> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
        this.setNoGravity(true);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getItem() {
        return EEEItems.CRESCENT_DISC.get().getDefaultInstance();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        if (--this.lifeTicks <= 0) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() instanceof LivingEntity target) {
            target.setDeltaMovement(target.getDeltaMovement().add(0, -1, 0));
        }
    }

    public static GallantSwingsEntity shoot(Level level, LivingEntity shooter, float power, double damage,
                                            int knockback) {
        GallantSwingsEntity swing = new GallantSwingsEntity(EEEntities.GALLANT_SWINGS.get(), shooter, level);

        swing.shoot(shooter.getLookAngle().x, shooter.getLookAngle().y, shooter.getLookAngle().z,
                power * 2.0F, 0.0F);
        swing.setNoGravity(true);
        swing.setBaseDamage(damage);
        swing.setKnockback(knockback);

        level.addFreshEntity(swing);

        return swing;
    }
}
