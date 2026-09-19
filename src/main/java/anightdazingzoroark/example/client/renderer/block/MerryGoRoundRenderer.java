package anightdazingzoroark.example.client.renderer.block;

import anightdazingzoroark.example.animatedblock.AnimatedMerryGoRoundBlock;
import anightdazingzoroark.example.client.model.block.MerryGoRoundBlockModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class MerryGoRoundRenderer extends GeoBlockRenderer<AnimatedMerryGoRoundBlock> {
    public MerryGoRoundRenderer() {
        super(new MerryGoRoundBlockModel(), AnimatedMerryGoRoundBlock::new);
    }

    @Override
    @NotNull
    public AxisAlignedBB getRenderBoundingBox(IBlockState state, BlockPos pos) {
        return new AxisAlignedBB(-1D, -0.5D, -1D, 2D, 2D, 2D).offset(pos);
    }
}
