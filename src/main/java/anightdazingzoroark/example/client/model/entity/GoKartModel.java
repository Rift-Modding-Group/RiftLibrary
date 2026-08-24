package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.GoKartEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    public String getTextureLocation(GoKartEntity object) {
        return "model/entity/go_kart.png";
    }

    @Override
    @NotNull
    public List<String> getAnimationIdentifiers(GoKartEntity animatable) {
        return List.of("animation.go_kart.move");
    }
}
