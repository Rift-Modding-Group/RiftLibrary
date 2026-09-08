package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedSimpleItemStack;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class SprinklerItemModel extends AnimatedGeoModel<AnimatedSimpleItemStack> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(AnimatedSimpleItemStack animatable) {
        return "geometry.sprinkler";
    }

    @Override
    @NotNull
    public String getTextureLocation(AnimatedSimpleItemStack animatable) {
        return "block/sprinkler.png";
    }
}
