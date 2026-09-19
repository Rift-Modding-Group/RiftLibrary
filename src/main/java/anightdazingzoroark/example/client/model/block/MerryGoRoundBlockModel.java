package anightdazingzoroark.example.client.model.block;

import anightdazingzoroark.example.animatedblock.AnimatedMerryGoRoundBlock;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class MerryGoRoundBlockModel extends AnimatedGeoModel<AnimatedMerryGoRoundBlock> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(AnimatedMerryGoRoundBlock object) {
        return "geometry.merry_go_round";
    }

    @Override
    @NotNull
    public String getTextureLocation(AnimatedMerryGoRoundBlock object) {
        return "block/merry_go_round.png";
    }
}
