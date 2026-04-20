package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedBubbleGunItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class BubbleGunModel extends AnimatedGeoModel<AnimatedBubbleGunItem> {
    @Override
    public ResourceLocation getModelLocation(AnimatedBubbleGunItem object) {
        return new ResourceLocation(RiftLib.ModID, "geo/bubble_gun.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AnimatedBubbleGunItem object) {
        return new ResourceLocation(RiftLib.ModID, "textures/item/bubble_gun.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AnimatedBubbleGunItem animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/bubble_gun.animation.json");
    }
}
