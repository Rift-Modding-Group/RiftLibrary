package anightdazingzoroark.example.client.model.tile;

import anightdazingzoroark.example.block.tile.MerryGoRoundTileEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MerryGoRoundBlockModel extends AnimatedGeoModel<MerryGoRoundTileEntity> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    public String getModelIdentifier(MerryGoRoundTileEntity object) {
        return "geometry.merry_go_round";
    }

    @Override
    public String getTextureLocation(MerryGoRoundTileEntity object) {
        return "block/merry_go_round.png";
    }

    @Override
    @NotNull
    public List<String> getAnimationIdentifiers(MerryGoRoundTileEntity animatable) {
        return List.of("animation.merry_go_round.rotate");
    }
}
