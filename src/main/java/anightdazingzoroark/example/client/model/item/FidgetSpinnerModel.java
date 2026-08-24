package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedFidgetSpinnerItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FidgetSpinnerModel extends AnimatedGeoModel<AnimatedFidgetSpinnerItem> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    public String getModelIdentifier(AnimatedFidgetSpinnerItem object) {
        return "geometry.fidget_spinner";
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
