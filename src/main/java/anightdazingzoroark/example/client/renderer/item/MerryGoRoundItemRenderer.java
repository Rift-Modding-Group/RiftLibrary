package anightdazingzoroark.example.client.renderer.item;

import anightdazingzoroark.example.client.model.item.MerryGoRoundItemModel;
import anightdazingzoroark.example.animateditem.AnimatedSimpleItemStack;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;
import net.minecraft.item.ItemStack;

public class MerryGoRoundItemRenderer extends GeoItemRenderer<AnimatedSimpleItemStack> {
    public MerryGoRoundItemRenderer() {
        super(new MerryGoRoundItemModel(), AnimatedSimpleItemStack::new);
    }
}
