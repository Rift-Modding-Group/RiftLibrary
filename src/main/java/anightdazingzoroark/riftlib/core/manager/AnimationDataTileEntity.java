package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

public class AnimationDataTileEntity extends AbstractAnimationData<TileEntity> {
    public AnimationDataTileEntity(TileEntity holder) {
        super(holder, getAnimatable(holder));
    }

    @Override
    public @NotNull NBTTagCompound asNBT() {
        return new NBTTagCompound();
    }

    private static IAnimatable<?> getAnimatable(TileEntity holder) {
        if (holder instanceof IAnimatable<?>) return (IAnimatable<?>) holder;
        throw new IllegalArgumentException("AnimationDataTileEntity holder must implement IAnimatableNew");
    }
}
