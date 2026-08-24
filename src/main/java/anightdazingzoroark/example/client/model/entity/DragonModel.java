package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.example.entity.DragonEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    @NotNull
    public List<String> getAnimationIdentifiers(DragonEntity animatable) {
        return List.of(
                "animation.dragon.flying", "animation.dragon.attack_while_flying",
                "animation.dragon.land_pose", "animation.dragon.walking",
                "animation.dragon.fly_pose", "animation.dragon.breathe_fire_while_flying",
                "animation.dragon.test"
        );
    }
}
