package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.block.AnimatedBlockStateHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class AnimationDataBlock extends AbstractAnimationData<AnimatedBlockStateHolder, AnimationDataBlock> {
    public AnimationDataBlock(AnimatedBlockStateHolder holder) {
        super(holder, holder);
    }

    @Override
    public void updateOnDataTick() {}

    @Override
    public boolean isValid() {
        return this.getHolder().isValid();
    }

    @Override
    @NotNull
    public NBTTagCompound getNBT() {
        NBTTagCompound tag = super.getNBT();
        tag.setString("AnimationTargetType", "Block");
        tag.setLong("BlockPos", this.getHolder().getPos().toLong());
        tag.setString("Block", this.getHolder().getBlockState().getBlock().getRegistryName().toString());
        tag.setInteger("Dimension", this.getWorld().provider.getDimension());
        return tag;
    }

    @Override
    public World getWorld() {
        return this.getHolder().getWorld();
    }
}
