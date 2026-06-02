package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.nbt.NBTTagCompound;
import org.jspecify.annotations.NonNull;

public class AnimationDataProjectile extends AbstractAnimationDataEntity<RiftLibProjectile, AnimationDataProjectile> {
    public AnimationDataProjectile(RiftLibProjectile holder) {
        super(holder, holder);
    }

    @Override
    @NonNull
    public NBTTagCompound asNBT() {
        NBTTagCompound toReturn = super.asNBT();
        toReturn.setString("AnimationTargetType", "Projectile");
        toReturn.setInteger("ProjectileID", this.getHolder().getEntityId());
        return toReturn;
    }
}