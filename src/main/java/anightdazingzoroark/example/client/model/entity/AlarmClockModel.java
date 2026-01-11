package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.AlarmClock;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class AlarmClockModel extends AnimatedGeoModel<AlarmClock> {
    @Override
    public ResourceLocation getModelLocation(AlarmClock object) {
        return new ResourceLocation(RiftLib.ModID, "geo/alarm_clock.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AlarmClock object) {
        return new ResourceLocation(RiftLib.ModID, "textures/model/entity/alarm_clock.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AlarmClock animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/alarm_clock.animation.json");
    }
}
