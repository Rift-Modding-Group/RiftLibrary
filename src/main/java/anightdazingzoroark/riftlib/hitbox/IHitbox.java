package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.lwjglx.util.vector.Quaternion;

/**
 * Shared methods between hitbox classes
 * */
public interface IHitbox<T extends IMultiHitboxUser<?>> {
    /**
     * Return the parent of the hitbox
     * */
    @NotNull
    T getParent();

    /**
     * Return the bounding box this hitbox is based on
     * */
    @NotNull
    AnimatedBoundingBox getBoundingBox();

    /**
     * Return the width and height based on parent scale and fixed size
     * */
    default float[] getHitboxSize() {
        return new float[]{
                this.getParent().multiHitboxUserScale() * this.getBoundingBox().getModelSpaceSize()[0] / 16f,
                this.getParent().multiHitboxUserScale() * this.getBoundingBox().getModelSpaceSize()[1] / 16f
        };
    }

    /**
     * Return the position of the hitbox based on parent position and yaw
     * and locator position
     * */
    @NotNull
    default Vec3d getHitboxPosition() {
        EntityLivingBase parentEntityLiving = this.getParent().getMultiHitboxUser();

        //correct the model space positions first
        Vec3d modelSpacePos = this.getBoundingBox().getModelSpacePosition();
        float[] modelSpaceSize = this.getBoundingBox().getModelSpaceSize();
        float[] modelSpaceSizeUnscaled = this.getBoundingBox().getUnscaledModelSpaceSize();

        float newHitboxX = (float) ((-modelSpacePos.x + modelSpaceSizeUnscaled[0] / 2f) / 16f);
        float newHitboxY = (float) ((modelSpacePos.y + modelSpaceSizeUnscaled[1] / 2f - modelSpaceSize[1] / 2f) / 16f);
        float newHitboxZ = (float) (-(modelSpacePos.z + modelSpaceSizeUnscaled[0] / 2f) / 16f);

        //set initial entity offset from center
        Vec3d posVec = new Vec3d(
                newHitboxX * this.getParent().multiHitboxUserScale(),
                newHitboxY * this.getParent().multiHitboxUserScale(),
                newHitboxZ * this.getParent().multiHitboxUserScale()
        );

        //determine yaw
        double normalYawRadians = -Math.toRadians(parentEntityLiving.rotationYawHead);
        double riddenYawRadians = -Math.toRadians(parentEntityLiving.rotationYaw);
        double finalYawRadians = parentEntityLiving.isBeingRidden() ? riddenYawRadians : normalYawRadians;

        //rotate vector around yaw
        Quaternion quaternion = QuaternionUtils.createXYZQuaternion(0, finalYawRadians, 0);
        posVec = VectorUtils.rotateVectorWithQuaternion(posVec, quaternion);

        //put in world
        return new Vec3d(
                parentEntityLiving.posX + posVec.x,
                parentEntityLiving.posY + posVec.y,
                parentEntityLiving.posZ + posVec.z
        );
    }
}
