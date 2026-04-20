package anightdazingzoroark.example.client.renderer.item;

import anightdazingzoroark.example.client.model.item.SprinklerItemModel;
import anightdazingzoroark.example.animateditem.AnimatedSimpleItemStack;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;
import net.minecraft.item.ItemStack;

public class SprinklerItemRenderer extends GeoItemRenderer<AnimatedSimpleItemStack> {
    public SprinklerItemRenderer() {
        super(new SprinklerItemModel(), AnimatedSimpleItemStack::new);
    }
}
