package anightdazingzoroark.example.client.renderer.item;

import anightdazingzoroark.example.client.model.item.FireworkStickModel;
import anightdazingzoroark.example.animateditem.AnimatedFireworkStickItem;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;
import net.minecraft.item.ItemStack;

public class FireworkStickRenderer extends GeoItemRenderer<AnimatedFireworkStickItem> {
    public FireworkStickRenderer() {
        super(new FireworkStickModel(), AnimatedFireworkStickItem::new);
    }
}
