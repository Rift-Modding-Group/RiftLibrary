package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.BombProjectile;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BombProjectileModel extends AnimatedGeoModel<BombProjectile> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    public String getModelIdentifier(BombProjectile object) {
        return "geometry.bomb";
    }

    @Override
    public ResourceLocation getTextureLocation(BombProjectile object) {
        return new ResourceLocation(RiftLib.ModID, "textures/model/entity/bomb.png");
    }

    @Override
    @NotNull
    public List<String> getAnimationIdentifiers(BombProjectile animatable) {
        return List.of("animation.bomb.flame_particles", "animation.bomb.sounds");
    }
}
