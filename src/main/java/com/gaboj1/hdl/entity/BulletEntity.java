
package com.gaboj1.hdl.entity;

import com.gaboj1.hdl.headshot.BoundingBoxManager;
import com.gaboj1.hdl.headshot.IHeadshotBox;
import com.gaboj1.hdl.init.HDLModEntities;
import com.gaboj1.hdl.init.HDLModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;

import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;

import java.util.Optional;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class BulletEntity extends AbstractArrow implements ItemSupplier {
	public BulletEntity(PlayMessages.SpawnEntity packet, Level world) {
		super(HDLModEntities.DESERT_EAGLE_BULLET.get(), world);
	}

	public BulletEntity(EntityType<? extends BulletEntity> type, Level world) {
		super(type, world);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ItemStack getItem() {
		return new ItemStack(HDLModItems.DESERT_EAGLE_BULLET.get());
	}

	@Override
	protected ItemStack getPickupItem() {
		return new ItemStack(HDLModItems.DESERT_EAGLE_AMMO.get());
	}

	@Override
	protected void doPostHurtEffects(LivingEntity entity) {
		super.doPostHurtEffects(entity);
		entity.setArrowCount(entity.getArrowCount() - 1);
	}

	@Override
	public void tick() {
		super.tick();
		if(this.tickCount>60){//不管在地上（保留弹孔）还是由于某种原因卡在天上（未知bug）统统灭了
			this.discard();
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult p_36755_) {
		super.onHitBlock(p_36755_);
		if (level().getBlockState(p_36755_.getBlockPos()).getBlock() instanceof AbstractGlassBlock ||
				level().getBlockState(p_36755_.getBlockPos()).getBlock() instanceof StainedGlassPaneBlock ||
					level().getBlockState(p_36755_.getBlockPos()).getBlock() instanceof IronBarsBlock ironBarsBlock &&ironBarsBlock.getName().toString().contains("block.minecraft.glass_pane")) {
			level().destroyBlock(p_36755_.getBlockPos(), true);
			this.discard();

			if (this.getOwner() instanceof ServerPlayer _player && distanceTo(_player) >= 100) {
				Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("holydinglegend:shoot_hundred_meters"));
				AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
				if (!_ap.isDone()) {
					for (String criteria : _ap.getRemainingCriteria())
						_player.getAdvancements().award(_adv, criteria);
				}
			}

		}
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		//TODO: 改成直接判断有无碰撞然后直接受伤，而不是调用父类的hit，不然射速快会受到原版生物的受伤冷却的影响导致吞子弹
		final Vec3 position = this.position();
		Entity entity = result.getEntity();
		AABB boundingBox = entity.getBoundingBox();
		Vec3 startVec = this.position();
		Vec3 endVec = startVec.add(this.getDeltaMovement());
		Vec3 hitPos = boundingBox.clip(startVec, endVec).orElse(null);
		/* Check for headshot */
		boolean headshot = false;
		if(entity instanceof LivingEntity)
		{
			IHeadshotBox<LivingEntity> headshotBox = (IHeadshotBox<LivingEntity>) BoundingBoxManager.getHeadshotBoxes(entity.getType());
			if(headshotBox != null)
			{
				AABB box = headshotBox.getHeadshotBox((LivingEntity) entity);
				if(box != null)
				{
					box = box.move(boundingBox.getCenter().x, boundingBox.minY, boundingBox.getCenter().z);
					Optional<Vec3> headshotHitPos = box.clip(startVec, endVec);
//					if(!headshotHitPos.isPresent())
//					{
//						box = box.inflate(Config.COMMON.gameplay.growBoundingBoxAmount.get(), 0, Config.COMMON.gameplay.growBoundingBoxAmount.get());
//						headshotHitPos = box.clip(startVec, endVec);
//					}
					if(headshotHitPos.isPresent() && (hitPos == null || headshotHitPos.get().distanceTo(hitPos) < 0.5))
					{
						hitPos = headshotHitPos.get();
						headshot = true;
					}
					if(headshot){
						if(entity instanceof Player player){
							setBaseDamage(getBaseDamage()*(player.getMaxHealth()/0.45));
						}else {
							setBaseDamage(getBaseDamage()*2);
						}

						if(level() instanceof ServerLevel level){
							level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, this.getOwner().position(), Vec2.ZERO, level, 4, "", Component.literal(""), level.getServer(), null).withSuppressedOutput(),
									"title @p actionbar \"§c§l"+I18n.get("info.simpledeserteagle.headshot") +"\"");

						}
					}
				}
			}
		}

		super.onHitEntity(result);
		if (this.getOwner() instanceof ServerPlayer _player && entity.distanceTo(_player) >= 100) {
			Advancement _adv = _player.server.getAdvancements().getAdvancement(new ResourceLocation("holydinglegend:shoot_hundred_meters"));
			AdvancementProgress _ap = _player.getAdvancements().getOrStartProgress(_adv);
			if (!_ap.isDone()) {
				for (String criteria : _ap.getRemainingCriteria())
					_player.getAdvancements().award(_adv, criteria);
			}
		}

	}

}
