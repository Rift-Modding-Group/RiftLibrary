package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedBombItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class BombModel extends AnimatedGeoModel<AnimatedBombItem> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(AnimatedBombItem object) {
        return "geometry.bomb";
    }

    @Override
    @NotNull
    public String getTextureLocation(AnimatedBombItem object) {
        return "model/entity/bomb.png";
    }
}
