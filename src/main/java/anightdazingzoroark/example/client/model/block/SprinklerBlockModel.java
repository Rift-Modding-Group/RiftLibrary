package anightdazingzoroark.example.client.model.block;

import anightdazingzoroark.example.animatedblock.AnimatedSprinklerBlock;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class SprinklerBlockModel extends AnimatedGeoModel<AnimatedSprinklerBlock> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(AnimatedSprinklerBlock object) {
        return "geometry.sprinkler";
    }

    @Override
    @NotNull
    public String getTextureLocation(AnimatedSprinklerBlock object) {
        return "block/sprinkler.png";
    }
}
