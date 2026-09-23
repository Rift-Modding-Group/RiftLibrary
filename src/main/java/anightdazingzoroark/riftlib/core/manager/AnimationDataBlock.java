package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.block.AnimatedBlockStateHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class AnimationDataBlock extends AbstractAnimationData<AnimatedBlockStateHolder, AnimationDataBlock> {
    private Function<AnimatedBlockStateHolder, Float> holderScale;

    public AnimationDataBlock(AnimatedBlockStateHolder holder) {
        super(holder, holder);
    }

    @Override
    public void updateOnDataTick() {}

    public void setScale(float value) {
        this.setScale(block -> value);
    }

    public void setScale(@NotNull Function<AnimatedBlockStateHolder, Float> holderScale) {
        this.holderScale = holderScale;
    }

    /**
     * The basis of model scaling of the block.
     * */
    public float getScale() {
        return this.holderScale == null ? 1f : this.holderScale.apply(this.getHolder());
    }

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
