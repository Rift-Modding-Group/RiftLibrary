package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedFireworkStickItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class FireworkStickModel extends AnimatedGeoModel<AnimatedFireworkStickItem> {
    @Override
    public ResourceLocation getModelLocation(AnimatedFireworkStickItem object) {
        return new ResourceLocation(RiftLib.ModID, "geo/firework_stick.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AnimatedFireworkStickItem object) {
        return new ResourceLocation(RiftLib.ModID, "textures/item/firework_stick.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AnimatedFireworkStickItem animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/firework_stick.animation.json");
    }
}
