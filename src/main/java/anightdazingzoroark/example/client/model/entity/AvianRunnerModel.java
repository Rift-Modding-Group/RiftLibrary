package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.AvianRunnerEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class AvianRunnerModel extends AnimatedGeoModel<AvianRunnerEntity> {
    @Override
    public ResourceLocation getModelLocation(AvianRunnerEntity object) {
        return new ResourceLocation(RiftLib.ModID, "geo/avian_runner.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AvianRunnerEntity object) {
        return new ResourceLocation(RiftLib.ModID, "textures/model/entity/avian_runner.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AvianRunnerEntity animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/avian_runner.animation.json");
    }
}
