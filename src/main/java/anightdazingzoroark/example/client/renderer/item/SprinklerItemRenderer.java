package anightdazingzoroark.example.client.renderer.item;

import anightdazingzoroark.example.client.model.item.SprinklerItemModel;
import anightdazingzoroark.example.item.AnimatedItemBlock;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;

public class SprinklerItemRenderer extends GeoItemRenderer<AnimatedItemBlock> {
    public SprinklerItemRenderer() {
        super(new SprinklerItemModel());
    }
}
