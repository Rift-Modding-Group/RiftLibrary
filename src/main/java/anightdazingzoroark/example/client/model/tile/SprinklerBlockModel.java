package anightdazingzoroark.example.client.model.tile;

import anightdazingzoroark.example.block.tile.SprinklerTileEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class SprinklerBlockModel extends AnimatedGeoModel<SprinklerTileEntity> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(SprinklerTileEntity object) {
        return "geometry.sprinkler";
    }

    @Override
    @NotNull
    public String getTextureLocation(SprinklerTileEntity object) {
        return "block/sprinkler.png";
    }
}
