package anightdazingzoroark.example.client.model.tile;

import anightdazingzoroark.example.block.tile.MerryGoRoundTileEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class MerryGoRoundModel extends AnimatedGeoModel<MerryGoRoundTileEntity> {
    @Override
    public ResourceLocation getModelLocation(MerryGoRoundTileEntity object) {
        return new ResourceLocation(RiftLib.ModID, "geo/merry_go_round.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(MerryGoRoundTileEntity object) {
        return new ResourceLocation(RiftLib.ModID, "textures/block/merry_go_round.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(MerryGoRoundTileEntity animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/merry_go_round.animation.json");
    }
}
