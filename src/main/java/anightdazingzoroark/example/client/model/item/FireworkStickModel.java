package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedFireworkStickItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class FireworkStickModel extends AnimatedGeoModel<AnimatedFireworkStickItem> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(AnimatedFireworkStickItem object) {
        return "geometry.firework_stick";
    }

    @Override
    @NotNull
    public String getTextureLocation(AnimatedFireworkStickItem object) {
        return "item/firework_stick.png";
    }
}
