package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.GoKart;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class GoKartModel extends AnimatedGeoModel<GoKart> {
    @Override
    public ResourceLocation getModelLocation(GoKart object) {
        return new ResourceLocation(RiftLib.ModID, "geo/go_kart.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(GoKart object) {
        return new ResourceLocation(RiftLib.ModID, "textures/model/entity/go_kart.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(GoKart animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/go_kart.animation.json");
    }
}
