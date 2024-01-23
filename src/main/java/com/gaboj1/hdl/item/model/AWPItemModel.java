package com.gaboj1.hdl.item.model;

import com.gaboj1.hdl.item.AWPItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AWPItemModel extends GeoModel<AWPItem> {
    private String textureResourceLocation = "";
    public AWPItemModel(String textureResourceLocation){
        this.textureResourceLocation = textureResourceLocation;
    }

    @Override
    public ResourceLocation getModelResource(AWPItem awpItem) {
        return new ResourceLocation("holydinglegend", "geo/awp.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AWPItem awpItem) {
        return new ResourceLocation("holydinglegend", textureResourceLocation);
    }

    @Override
    public ResourceLocation getAnimationResource(AWPItem awpItem) {
        return new ResourceLocation("holydinglegend", "animations/awp.animation.json");
    }
}
