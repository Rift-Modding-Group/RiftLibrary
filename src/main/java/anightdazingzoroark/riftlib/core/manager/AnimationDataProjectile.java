package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.nbt.NBTTagCompound;
import org.jspecify.annotations.NonNull;

public class AnimationDataProjectile extends AbstractAnimationData<RiftLibProjectile> {
    public AnimationDataProjectile(RiftLibProjectile holder) {
        super(holder, getAnimatable(holder));
    }

    @Override
    @NonNull
    public NBTTagCompound asNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        toReturn.setInteger("ProjectileID", this.getHolder().getEntityId());
        return toReturn;
    }

    public boolean isMoving() {
        return !this.getHolder().onGround;
    }

    private static IAnimatable<?> getAnimatable(RiftLibProjectile holder) {
        if (holder instanceof IAnimatable<?>) return (IAnimatable<?>) holder;
        throw new IllegalArgumentException("AnimationDataProjectile holder must implement IAnimatableNew");
    }
}
