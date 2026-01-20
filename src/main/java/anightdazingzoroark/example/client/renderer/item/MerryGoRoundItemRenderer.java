package anightdazingzoroark.example.client.renderer.item;

import anightdazingzoroark.example.client.model.item.MerryGoRoundItemModel;
import anightdazingzoroark.example.item.AnimatedItemBlock;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;

public class MerryGoRoundItemRenderer extends GeoItemRenderer<AnimatedItemBlock> {
    public MerryGoRoundItemRenderer() {
        super(new MerryGoRoundItemModel());
    }
}
