package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedFidgetSpinnerItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class FidgetSpinnerModel extends AnimatedGeoModel<AnimatedFidgetSpinnerItem> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(AnimatedFidgetSpinnerItem object) {
        return "geometry.fidget_spinner";
    }

    @Override
    @NotNull
    public String getTextureLocation(AnimatedFidgetSpinnerItem object) {
        return "item/fidget_spinner.png";
    }
}
