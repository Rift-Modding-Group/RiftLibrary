package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.util.MiscUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import org.jspecify.annotations.NonNull;

public class AnimationDataEntity extends AbstractAnimationData<EntityLivingBase> {
    public AnimationDataEntity(EntityLivingBase holder) {
        super(holder, getAnimatable(holder));
    }

    @Override
    @NonNull
    public NBTTagCompound asNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        toReturn.setInteger("EntityID", this.getHolder().getEntityId());
        return toReturn;
    }

    public boolean isMoving() {
        return this.getHorizontalSpeed() > 0;
    }

    public double getHorizontalSpeed() {
        return MiscUtils.getEntityHorizontalSpeed(this.getHolder());
    }

    private static IAnimatable<?> getAnimatable(EntityLivingBase holder) {
        if (holder instanceof IAnimatable<?>) return (IAnimatable<?>) holder;
        throw new IllegalArgumentException("AnimationDataEntity holder must implement IAnimatableNew");
    }
}
