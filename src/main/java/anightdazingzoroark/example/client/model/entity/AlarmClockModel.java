package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.AlarmClockEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class AlarmClockModel extends AnimatedGeoModel<AlarmClockEntity> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    public String getModelIdentifier(AlarmClockEntity object) {
        return "geometry.alarm_clock";
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
