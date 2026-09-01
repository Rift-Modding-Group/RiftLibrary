package anightdazingzoroark.example.client.model.tile;

import anightdazingzoroark.example.block.tile.SprinklerTileEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SprinklerBlockModel extends AnimatedGeoModel<SprinklerTileEntity> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    public String getModelIdentifier(SprinklerTileEntity object) {
        return "geometry.sprinkler";
    }

    @Override
    public String getTextureLocation(SprinklerTileEntity object) {
        return "block/sprinkler.png";
    }
}
