package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.AlarmClockEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class AlarmClockModel extends AnimatedGeoModel<AlarmClockEntity> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(AlarmClockEntity object) {
        return "geometry.alarm_clock";
    }

    @Override
    @NotNull
    public String getTextureLocation(AlarmClockEntity object) {
        return "model/entity/alarm_clock.png";
    }
}
