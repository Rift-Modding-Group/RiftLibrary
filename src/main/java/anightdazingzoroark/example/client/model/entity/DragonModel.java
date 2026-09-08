package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.DragonEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class DragonModel extends AnimatedGeoModel<DragonEntity> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(DragonEntity object) {
        return "geometry.dragon";
    }

    @Override
    @NotNull
    public String getTextureLocation(DragonEntity object) {
        return "model/entity/dragon.png";
    }
}
