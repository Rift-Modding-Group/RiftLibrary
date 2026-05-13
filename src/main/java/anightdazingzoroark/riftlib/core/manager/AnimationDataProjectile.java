package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import anightdazingzoroark.riftlib.util.MiscUtils;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.function.BiFunction;

public class AnimationDataProjectile extends AbstractAnimationDataEntity<RiftLibProjectile<?>> {
    public AnimationDataProjectile(RiftLibProjectile<?> holder) {
        super(holder, getAnimatable(holder));
    }

    @Override
    @NonNull
    public NBTTagCompound asNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        toReturn.setString("AnimationTargetType", "Projectile");
        toReturn.setInteger("ProjectileID", this.getHolder().getEntityId());
        return toReturn;
    }

    private static IAnimatable<?> getAnimatable(RiftLibProjectile<?> holder) {
        if (holder instanceof IAnimatable<?>) return (IAnimatable<?>) holder;
        throw new IllegalArgumentException("AnimationDataProjectile holder must implement IAnimatableNew");
    }
}
