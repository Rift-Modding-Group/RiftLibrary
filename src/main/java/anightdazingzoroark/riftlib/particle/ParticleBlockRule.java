package anightdazingzoroark.riftlib.particle;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class ParticleBlockRule {
    public final Block block;
    public final int meta; // -1 means wildcard

    public ParticleBlockRule(Block block, int meta) {
        this.block = block;
        this.meta = meta;
    }

    public boolean matches(IBlockState state) {
        if (state.getBlock() != block) return false;
        if (this.meta == -1) return true;
        return state.getBlock().getMetaFromState(state) == this.meta;
    }
}
