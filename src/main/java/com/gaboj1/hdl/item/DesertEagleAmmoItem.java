package com.gaboj1.hdl.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;

import java.util.List;

public class DesertEagleAmmoItem extends Item {
	public DesertEagleAmmoItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON));
	}

}
