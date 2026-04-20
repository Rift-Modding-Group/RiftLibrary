package anightdazingzoroark.example.client.renderer.item;

import anightdazingzoroark.example.client.model.item.FidgetSpinnerModel;
import anightdazingzoroark.example.animateditem.AnimatedFidgetSpinnerItem;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;
import net.minecraft.item.ItemStack;

public class FidgetSpinnerRenderer extends GeoItemRenderer<AnimatedFidgetSpinnerItem> {
    public FidgetSpinnerRenderer() {
        super(new FidgetSpinnerModel(), AnimatedFidgetSpinnerItem::new);
    }
}
