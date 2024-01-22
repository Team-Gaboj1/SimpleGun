package com.gaboj1.hdl.entity;

import com.gaboj1.hdl.headshot.BoundingBoxManager;
import com.gaboj1.hdl.headshot.IHeadshotBox;
import com.gaboj1.hdl.init.HDLModItems;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

public class AWPBulletEntity extends AbstractArrow implements ItemSupplier {
    protected AWPBulletEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack getItem() {
        return new ItemStack(HDLModItems.AWP.get());
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(HDLModItems.AWP_AMMO.get());
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        if (level().getBlockState(pResult.getBlockPos()).getBlock() instanceof AbstractGlassBlock ||
                level().getBlockState(pResult.getBlockPos()).getBlock() instanceof StainedGlassPaneBlock ||
                level().getBlockState(pResult.getBlockPos()).getBlock() instanceof StainedGlassBlock)
            level().destroyBlock(pResult.getBlockPos(), true);
        this.discard();

        super.onHitBlock(pResult);
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        final Vec3 position = this.position();
        Entity entity = pResult.getEntity();
        AABB boundingBox = entity.getBoundingBox();
        Vec3 startVec = this.position();
        Vec3 endVec = startVec.add(this.getDeltaMovement());
        Vec3 hitPos = boundingBox.clip(startVec, endVec).orElse(null);

        boolean headshot = false;
        if (entity instanceof LivingEntity) {
            IHeadshotBox<LivingEntity> headshotBox = (IHeadshotBox<LivingEntity>) BoundingBoxManager.getHeadshotBoxes(entity.getType());
            if (headshotBox != null) {
                AABB box = headshotBox.getHeadshotBox((LivingEntity) entity);
                if (box != null) {
                    box = box.move(boundingBox.getCenter().x, boundingBox.minY, boundingBox.getCenter().z);
                    Optional<Vec3> headshotHitPos = box.clip(startVec, endVec);
                    if (headshotHitPos.isPresent() && (hitPos == null || headshotHitPos.get().distanceTo(hitPos) < 0.5)) {
                        hitPos = headshotHitPos.get();
                        headshot = true;
                    }
                    super.onHitEntity(pResult);
                    if (headshot) {
                        if (entity instanceof Player player) {
                            setBaseDamage(getBaseDamage() * (player.getMaxHealth() / 0.45));
                        } else {
                            setBaseDamage(getBaseDamage() * 2);
                        }

                        if (level() instanceof ServerLevel level) {
                            level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, this.getOwner().position(), Vec2.ZERO, level, 4, "", Component.literal(""), level.getServer(), null).withSuppressedOutput(),
                                    "title @p actionbar \"§c§l" + I18n.get("info.simpledeserteagle.headshot") + "\"");

                        }
                    }
                }
            }
        }
    }
}