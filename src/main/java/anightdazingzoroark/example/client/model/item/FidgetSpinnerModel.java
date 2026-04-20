package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedFidgetSpinnerItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class FidgetSpinnerModel extends AnimatedGeoModel<AnimatedFidgetSpinnerItem> {
    @Override
    public ResourceLocation getModelLocation(AnimatedFidgetSpinnerItem object) {
        return new ResourceLocation(RiftLib.ModID, "geo/fidget_spinner.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AnimatedFidgetSpinnerItem object) {
        return new ResourceLocation(RiftLib.ModID, "textures/item/fidget_spinner.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AnimatedFidgetSpinnerItem animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/fidget_spinner.animation.json");
    }
}
