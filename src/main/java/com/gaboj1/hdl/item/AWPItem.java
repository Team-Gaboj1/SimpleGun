package com.gaboj1.hdl.item;

import com.gaboj1.hdl.HDLMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AWPItem extends Item{
    public final static int DURABILITY = 10; //耐久

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, HDLMod.MOD_ID);

    public static final RegistryObject<Item> AWP = ITEMS.register("awp",
            () -> new Item(new Item.Properties()));

    public AWPItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC).defaultDurability(DURABILITY));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("tooltip.holydinglegend.awp.tooltip"));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
