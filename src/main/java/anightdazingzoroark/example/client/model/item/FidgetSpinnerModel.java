package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedFidgetSpinnerItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    public String getTextureLocation(AnimatedFidgetSpinnerItem object) {
        return "item/fidget_spinner.png";
    }

    @Override
    @NotNull
    public List<String> getAnimationIdentifiers(AnimatedFidgetSpinnerItem animatable) {
        return List.of("animation.fidget_spinner.spin");
    }
}
