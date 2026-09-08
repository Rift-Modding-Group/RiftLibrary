package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.FlyingPufferfishEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class FlyingPufferfishModel extends AnimatedGeoModel<FlyingPufferfishEntity> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(FlyingPufferfishEntity object) {
        return "geometry.flying_pufferfish";
    }

    @Override
    @NotNull
    public String getTextureLocation(FlyingPufferfishEntity object) {
        return "model/entity/flying_pufferfish.png";
    }
}
