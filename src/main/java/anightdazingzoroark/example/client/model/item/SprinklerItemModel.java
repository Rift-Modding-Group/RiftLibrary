package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.item.AnimatedItemBlock;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class SprinklerItemModel extends AnimatedGeoModel<AnimatedItemBlock> {
    @Override
    public ResourceLocation getModelLocation(AnimatedItemBlock animatable) {
        return new ResourceLocation(RiftLib.ModID, "geo/sprinkler.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AnimatedItemBlock animatable) {
        return new ResourceLocation(RiftLib.ModID, "textures/block/sprinkler.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AnimatedItemBlock animatable) {
        return null;
    }
}
