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
    public String getTextureLocation(BombProjectile object) {
        return "model/entity/bomb.png";
    }
}
