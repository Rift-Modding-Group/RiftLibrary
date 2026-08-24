package anightdazingzoroark.example.client.model.item;

import anightdazingzoroark.example.animateditem.AnimatedSimpleItemStack;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MerryGoRoundItemModel extends AnimatedGeoModel<AnimatedSimpleItemStack> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    public String getModelIdentifier(AnimatedSimpleItemStack animatable) {
        return "geometry.merry_go_round";
    }

    @Override
    public String getTextureLocation(AnimatedSimpleItemStack animatable) {
        return "block/merry_go_round.png";
    }

    @Override
    @NotNull
    public List<String> getAnimationIdentifiers(AnimatedSimpleItemStack animatable) {
        return List.of();
    }
}
