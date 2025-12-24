package anightdazingzoroark.example.client.renderer.item;

import anightdazingzoroark.example.client.model.item.BubbleGunModel;
import anightdazingzoroark.example.item.BubbleGunItem;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;

public class BubbleGunRenderer extends GeoItemRenderer<BubbleGunItem> {
    public BubbleGunRenderer() {
        super(new BubbleGunModel());
    }
}
