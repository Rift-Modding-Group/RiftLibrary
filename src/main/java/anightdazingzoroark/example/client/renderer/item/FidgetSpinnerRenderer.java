package anightdazingzoroark.example.client.renderer.item;

import anightdazingzoroark.example.client.model.item.FidgetSpinnerModel;
import anightdazingzoroark.example.item.FidgetSpinnerItem;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;

public class FidgetSpinnerRenderer extends GeoItemRenderer<FidgetSpinnerItem> {
    public FidgetSpinnerRenderer() {
        super(new FidgetSpinnerModel());
    }
}
