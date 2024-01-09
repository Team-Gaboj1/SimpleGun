package com.gaboj1.hdl;

import org.lwjgl.glfw.GLFW;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class KeyMappingsTest {
	public static final KeyMapping RELOAD = new KeyMapping("key.simpledeserteagle.reload", GLFW.GLFW_KEY_R, "key.categories.misc") {
		private boolean isDownOld = false;
		private boolean isReloading = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown &&!isReloading) {
				isReloading = true;
				HDLMod.PACKET_HANDLER.sendToServer(new ReloadMessage(0, 0));
				ReloadMessage.pressAction(Minecraft.getInstance().player, 0);
				new Thread(()->{
					try {
						Thread.sleep(2000);
						isReloading = false;
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}).start();
			}
			isDownOld = isDown;
		}
	};

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(RELOAD);
	}

	@Mod.EventBusSubscriber({Dist.CLIENT})
	public static class KeyEventListener {
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event) {
			if (Minecraft.getInstance().screen == null) {
				RELOAD.consumeClick();
			}
		}
	}
}
