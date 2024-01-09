package com.gaboj1.hdl.item.renderer;

import com.gaboj1.hdl.item.DesertEagleItem;
import com.gaboj1.hdl.item.model.DesertEagleItemModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class DesertEagleItemRenderer extends GeoItemRenderer<DesertEagleItem> {
    public DesertEagleItemRenderer() {
        super(new DesertEagleItemModel("textures/item/texturecrc.png"));
    }

}
