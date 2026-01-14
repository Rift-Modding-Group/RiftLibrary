package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.AlarmClockEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class AlarmClockModel extends AnimatedGeoModel<AlarmClockEntity> {
    @Override
    public ResourceLocation getModelLocation(AlarmClockEntity object) {
        return new ResourceLocation(RiftLib.ModID, "geo/alarm_clock.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AlarmClockEntity object) {
        return new ResourceLocation(RiftLib.ModID, "textures/model/entity/alarm_clock.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AlarmClockEntity animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/alarm_clock.animation.json");
    }
}
