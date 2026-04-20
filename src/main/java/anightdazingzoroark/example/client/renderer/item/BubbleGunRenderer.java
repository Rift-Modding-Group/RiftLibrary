package anightdazingzoroark.example.client.renderer.item;

import anightdazingzoroark.example.client.model.item.BubbleGunModel;
import anightdazingzoroark.example.animateditem.AnimatedBubbleGunItem;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRenderer;
import net.minecraft.item.ItemStack;

public class BubbleGunRenderer extends GeoItemRenderer<AnimatedBubbleGunItem> {
    public BubbleGunRenderer() {
        super(new BubbleGunModel(), AnimatedBubbleGunItem::new);
    }
}
