package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.DragonEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DragonModel extends AnimatedGeoModel<DragonEntity> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    public String getModelIdentifier(DragonEntity object) {
        return "geometry.dragon";
    }

    @Override
    public ResourceLocation getTextureLocation(DragonEntity object) {
        return new ResourceLocation(RiftLib.ModID, "textures/model/entity/dragon.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(DragonEntity animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/dragon.animation.json");
    }
}
