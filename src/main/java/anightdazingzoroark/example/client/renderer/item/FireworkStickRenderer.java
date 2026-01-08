package anightdazingzoroark.example.client.renderer.item;

import anightdazingzoroark.example.client.model.item.FireworkStickModel;
import anightdazingzoroark.example.item.FireworkStickItem;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;

public class FireworkStickRenderer extends GeoItemRenderer<FireworkStickItem> {
    public FireworkStickRenderer() {
        super(new FireworkStickModel());
    }
}
