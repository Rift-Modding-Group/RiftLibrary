package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.AvianRunnerEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class AvianRunnerModel extends AnimatedGeoModel<AvianRunnerEntity> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(AvianRunnerEntity object) {
        return "geometry.avian_runner";
    }

    @Override
    @NotNull
    public String getTextureLocation(AvianRunnerEntity object) {
        return "model/entity/avian_runner.png";
    }
}
