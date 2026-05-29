package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class AnimationDataTileEntity extends AbstractAnimationData<TileEntity> {
    public AnimationDataTileEntity(TileEntity holder) {
        super(holder, getAnimatable(holder));
    }

    @Override
    public void updateOnDataTick() {}

    @Override
    public boolean isValid() {
        return !this.getHolder().isInvalid();
    }

    @Override
    public @NotNull NBTTagCompound asNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        BlockPos tileEntityPos = this.getHolder().getPos();
        toReturn.setString("AnimationTargetType", "TileEntity");
        toReturn.setIntArray("TileEntityPos", new int[]{tileEntityPos.getX(), tileEntityPos.getY(), tileEntityPos.getZ()});
        return toReturn;
    }

    @Override
    public World getWorld() {
        return this.getHolder().getWorld();
    }

    private static IAnimatable<?> getAnimatable(TileEntity holder) {
        if (holder instanceof IAnimatable<?>) return (IAnimatable<?>) holder;
        throw new IllegalArgumentException("AnimationDataTileEntity holder must implement IAnimatableNew");
    }
}
