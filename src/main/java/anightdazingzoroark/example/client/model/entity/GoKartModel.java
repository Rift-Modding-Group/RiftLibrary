package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.GoKartEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GoKartModel extends AnimatedGeoModel<GoKartEntity> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    public String getModelIdentifier(GoKartEntity object) {
        return "geometry.go_kart";
    }

    @Override
    public ResourceLocation getTextureLocation(GoKartEntity object) {
        return new ResourceLocation(RiftLib.ModID, "textures/model/entity/go_kart.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(GoKartEntity animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/go_kart.animation.json");
    }
}
