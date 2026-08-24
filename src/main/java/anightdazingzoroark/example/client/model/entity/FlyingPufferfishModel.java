package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.FlyingPufferfishEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FlyingPufferfishModel extends AnimatedGeoModel<FlyingPufferfishEntity> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    public String getModelIdentifier(FlyingPufferfishEntity object) {
        return "geometry.flying_pufferfish";
    }

    @Override
    public ResourceLocation getTextureLocation(FlyingPufferfishEntity object) {
        return new ResourceLocation(RiftLib.ModID, "textures/model/entity/flying_pufferfish.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(FlyingPufferfishEntity animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/flying_pufferfish.animation.json");
    }
}
