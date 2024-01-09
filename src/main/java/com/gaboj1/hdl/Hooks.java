package com.gaboj1.hdl;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.pinero.simpledeserteagle.event.PlayerModelEvent;

public class Hooks {
    public static void fireRenderPlayer(EntityModel model, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha, LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if(entity instanceof Player && model instanceof PlayerModel)
        {
            if(!MinecraftForge.EVENT_BUS.post(new PlayerModelEvent.Render.Pre((Player) entity, (PlayerModel) model, poseStack, consumer, light, overlay, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, Minecraft.getInstance().getDeltaFrameTime())))
            {
                model.renderToBuffer(poseStack, consumer, light, overlay, red, green, blue, alpha);
                MinecraftForge.EVENT_BUS.post(new PlayerModelEvent.Render.Post((Player) entity, (PlayerModel) model, poseStack, consumer, light, overlay, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, Minecraft.getInstance().getDeltaFrameTime()));
            }
            return;
        }
        model.renderToBuffer(poseStack, consumer, light, overlay, red, green, blue, alpha);
    }
}
