package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.GoKartEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class GoKartModel extends AnimatedGeoModel<GoKartEntity> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(GoKartEntity object) {
        return "geometry.go_kart";
    }

    @Override
    @NotNull
    public String getTextureLocation(GoKartEntity object) {
        return "model/entity/go_kart.png";
    }
}
