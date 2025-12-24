package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.item.BubbleGunItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class BubbleGunModel extends AnimatedGeoModel<BubbleGunItem> {
    @Override
    public ResourceLocation getModelLocation(BubbleGunItem object) {
        return new ResourceLocation(RiftLib.ModID, "geo/bubble_gun.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(BubbleGunItem object) {
        return new ResourceLocation(RiftLib.ModID, "textures/item/bubble_gun.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(BubbleGunItem animatable) {
        return null;
    }
}
