package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedSimpleItemStack;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class SprinklerItemModel extends AnimatedGeoModel<AnimatedSimpleItemStack> {
    @Override
    public ResourceLocation getModelLocation(AnimatedSimpleItemStack animatable) {
        return new ResourceLocation(RiftLib.ModID, "geo/sprinkler.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AnimatedSimpleItemStack animatable) {
        return new ResourceLocation(RiftLib.ModID, "textures/block/sprinkler.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AnimatedSimpleItemStack animatable) {
        return null;
    }
}
