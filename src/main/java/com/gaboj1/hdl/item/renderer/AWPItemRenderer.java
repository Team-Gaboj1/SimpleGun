package com.gaboj1.hdl.item.renderer;

import com.gaboj1.hdl.item.AWPItem;
import com.gaboj1.hdl.item.model.AWPItemModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class AWPItemRenderer extends GeoItemRenderer<AWPItem> {
    public AWPItemRenderer() {
        super(new AWPItemModel("textures/item/awp.png"));
    }
}
