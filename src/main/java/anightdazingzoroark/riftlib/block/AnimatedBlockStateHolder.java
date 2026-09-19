package anightdazingzoroark.riftlib.block;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AnimatedBlockStateHolder implements IAnimatable<AnimationDataBlock> {
    private final World world;
    @NotNull
    private final BlockPos pos;
    @NotNull
    private IBlockState blockState;
    private AnimationDataBlock animationData;
    private boolean valid = true;

    protected AnimatedBlockStateHolder(World world, @NotNull BlockPos pos, @NotNull IBlockState blockState) {
        this.world = world;
        this.pos = pos.toImmutable();
        this.blockState = blockState;
    }

    public World getWorld() {
        return this.world;
    }

    @NotNull
    public BlockPos getPos() {
        return this.pos;
    }

    @NotNull
    public IBlockState getBlockState() {
        return this.blockState;
    }

    @Nullable
    public TileEntity getTileEntity() {
        return this.world.getTileEntity(this.pos);
    }

    public void setBlockState(IBlockState blockState) {
        if (blockState.getBlock() != this.blockState.getBlock()) throw new IllegalArgumentException("Cannot change a holder's block type");
        this.blockState = blockState;
    }

    public boolean isValid() {
        return this.valid;
    }

    public void invalidate() {
        this.valid = false;
    }

    @Override
    @NotNull
    public AnimationDataBlock getAnimationData() {
        if (this.animationData == null) this.animationData = new AnimationDataBlock(this);
        return this.animationData;
    }
}
