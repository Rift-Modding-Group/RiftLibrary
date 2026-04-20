package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedBombItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class BombModel extends AnimatedGeoModel<AnimatedBombItem> {
    @Override
    public ResourceLocation getModelLocation(AnimatedBombItem object) {
        return new ResourceLocation(RiftLib.ModID, "geo/bomb.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AnimatedBombItem object) {
        return new ResourceLocation(RiftLib.ModID, "textures/model/entity/bomb.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AnimatedBombItem animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/bomb.animation.json");
    }
}
