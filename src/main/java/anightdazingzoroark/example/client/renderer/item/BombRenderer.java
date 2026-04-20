package anightdazingzoroark.example.client.renderer.item;

import anightdazingzoroark.example.client.model.item.BombModel;
import anightdazingzoroark.example.animateditem.AnimatedBombItem;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;
import net.minecraft.item.ItemStack;

public class BombRenderer extends GeoItemRenderer<AnimatedBombItem> {
    public BombRenderer() {
        super(new BombModel(), AnimatedBombItem::new);
    }
}
