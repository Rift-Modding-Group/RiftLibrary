package anightdazingzoroark.example.client.model.tile;

import anightdazingzoroark.example.block.tile.MerryGoRoundTileEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class MerryGoRoundBlockModel extends AnimatedGeoModel<MerryGoRoundTileEntity> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(MerryGoRoundTileEntity object) {
        return "geometry.merry_go_round";
    }

    @Override
    @NotNull
    public String getTextureLocation(MerryGoRoundTileEntity object) {
        return "block/merry_go_round.png";
    }
}
