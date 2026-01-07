package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.item.FidgetSpinnerItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class FidgetSpinnerModel extends AnimatedGeoModel<FidgetSpinnerItem> {
    @Override
    public ResourceLocation getModelLocation(FidgetSpinnerItem object) {
        return new ResourceLocation(RiftLib.ModID, "geo/fidget_spinner.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(FidgetSpinnerItem object) {
        return new ResourceLocation(RiftLib.ModID, "textures/item/fidget_spinner.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(FidgetSpinnerItem animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/fidget_spinner.animation.json");
    }
}
