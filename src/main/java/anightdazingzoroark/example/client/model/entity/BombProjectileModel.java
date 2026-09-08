package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.BombProjectile;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class BombProjectileModel extends AnimatedGeoModel<BombProjectile> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(BombProjectile object) {
        return "geometry.bomb";
    }

    @Override
    @NotNull
    public String getTextureLocation(BombProjectile object) {
        return "model/entity/bomb.png";
    }
}
