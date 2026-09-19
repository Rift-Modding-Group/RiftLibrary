package anightdazingzoroark.example.client.renderer.block;

import anightdazingzoroark.example.animatedblock.AnimatedSprinklerBlock;
import anightdazingzoroark.example.client.model.block.SprinklerBlockModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class SprinklerRenderer extends GeoBlockRenderer<AnimatedSprinklerBlock> {
    public SprinklerRenderer() {
        super(new SprinklerBlockModel(), AnimatedSprinklerBlock::new);
    }

    @Override
    @NotNull
    public AxisAlignedBB getRenderBoundingBox(IBlockState state, BlockPos pos) {
        return new AxisAlignedBB(-0.5, -0.5, -0.5, 1.5, 2, 1.5).offset(pos);
    }
}
