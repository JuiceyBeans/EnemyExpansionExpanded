package com.juiceybeans.eeexpanded.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;

public class CinderFireballEntity extends AbstractArrow implements ItemSupplier {
    public CinderFireballEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public CinderFireballEntity(EntityType<? extends AbstractArrow> entityType, double x, double y, double z, Level level) {
        super(entityType, x, y, z, level);
    }

    public CinderFireballEntity(EntityType<? extends AbstractArrow> entityType, LivingEntity shooter, Level level) {
        super(entityType, shooter, level);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getItem() {
        return Items.FIRE_CHARGE.getDefaultInstance();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!(result.getEntity() instanceof LivingEntity living && living.isBlocking())) result.getEntity().setSecondsOnFire(4);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        var x = result.getBlockPos().getX();
        var y = result.getBlockPos().getY();
        var z = result.getBlockPos().getZ();

        if (level().getLevelData().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).get()) {
            if (!level().isClientSide()) level().explode(this, x, y, z, 2.0F, Level.ExplosionInteraction.MOB);
        } else {
            if (!level().isClientSide()) level().explode(this, x, y, z, 2.0F, Level.ExplosionInteraction.NONE);
        }

        if (level().getBlockState(new BlockPos(x, y - 1, z)).is(Blocks.AIR)) {
            level().setBlock(new BlockPos(x, y, z), Blocks.FIRE.defaultBlockState(), 3);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        this.setSecondsOnFire(10);
    }
}
