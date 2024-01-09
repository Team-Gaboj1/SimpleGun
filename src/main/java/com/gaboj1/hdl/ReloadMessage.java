package com.gaboj1.hdl;

import com.gaboj1.hdl.item.DesertEagleItem;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ReloadMessage {
	int type, pressedms;

	public ReloadMessage(int type, int pressedms) {
		this.type = type;
		this.pressedms = pressedms;
	}

	public ReloadMessage(FriendlyByteBuf buffer) {
		this.type = buffer.readInt();
		this.pressedms = buffer.readInt();
	}

	public static void buffer(ReloadMessage message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.type);
		buffer.writeInt(message.pressedms);
	}

	public static void handler(ReloadMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			pressAction(context.getSender(), message.type);
		});
		context.setPacketHandled(true);
	}

	public static void pressAction(Player entity, int type) {
		Level world = entity.level();
		if (type == 0) {
			DesertEagleItem.reload(world, entity);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		HDLMod.addNetworkMessage(ReloadMessage.class, ReloadMessage::buffer, ReloadMessage::new, ReloadMessage::handler);
	}
}
