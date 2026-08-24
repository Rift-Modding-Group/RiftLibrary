package anightdazingzoroark.example.client.model.tile;

import anightdazingzoroark.example.block.tile.MerryGoRoundTileEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

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
    public ResourceLocation getTextureLocation(MerryGoRoundTileEntity object) {
        return new ResourceLocation(RiftLib.ModID, "textures/block/merry_go_round.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(MerryGoRoundTileEntity animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/merry_go_round.animation.json");
    }
}
