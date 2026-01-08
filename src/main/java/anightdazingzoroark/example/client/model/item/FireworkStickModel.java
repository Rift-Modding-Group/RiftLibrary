package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.item.FireworkStickItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class FireworkStickModel extends AnimatedGeoModel<FireworkStickItem> {
    @Override
    public ResourceLocation getModelLocation(FireworkStickItem object) {
        return new ResourceLocation(RiftLib.ModID, "geo/firework_stick.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(FireworkStickItem object) {
        return new ResourceLocation(RiftLib.ModID, "textures/item/firework_stick.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(FireworkStickItem animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/firework_stick.animation.json");
    }
}
