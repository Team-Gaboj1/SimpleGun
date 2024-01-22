package com.gaboj1.hdl.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class AWPAmmoItem extends Item {

    public AWPAmmoItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.COMMON));
    }
}
