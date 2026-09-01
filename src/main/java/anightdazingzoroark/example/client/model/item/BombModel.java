package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedBombItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BombModel extends AnimatedGeoModel<AnimatedBombItem> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    public String getModelIdentifier(AnimatedBombItem object) {
        return "geometry.bomb";
    }

    @Override
    public String getTextureLocation(AnimatedBombItem object) {
        return "model/entity/bomb.png";
    }
}
