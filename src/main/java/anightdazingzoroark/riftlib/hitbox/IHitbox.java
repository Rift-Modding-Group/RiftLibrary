package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.EntityLivingBase;
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
     * Return the locator this hitbox will be attached to
     * */
    @NotNull
    AnimatedLocator getHitboxLocator();

    /**
     * Return the fixed size of the hitbox. 0 is width, 1 is height
     * */
    float[] getFixedSize();

    /**
     * Return the width and height based on parent scale and fixed size
     * */
    default float[] getHitboxSize() {
        float locatorWidthDelta = Math.max(
                this.getHitboxLocator().getParentBone().getScale().x,
                this.getHitboxLocator().getParentBone().getScale().z
        );
        float locatorHeightDelta = this.getHitboxLocator().getParentBone().getScale().y;
        float finalWidth = this.getFixedSize()[0] * this.getParent().multiHitboxUserScale() * locatorWidthDelta;
        float finalHeight = this.getFixedSize()[1] * this.getParent().multiHitboxUserScale() * locatorHeightDelta;

        return new float[]{finalWidth, finalHeight};
    }

    /**
     * Return the position of the hitbox based on parent position and yaw
     * and locator position
     * */
    @NotNull
    default Vec3d getHitboxPosition() {
        EntityLivingBase parentEntityLiving = this.getParent().getMultiHitboxUser();

        //correct the model space positions first
        Vec3d modelSpacePos = this.getHitboxLocator().getModelSpacePosition();
        float newHitboxX = -(float) modelSpacePos.x / 16f;
        float newHitboxY = (float) modelSpacePos.y / 16f - (this.getFixedSize()[1] / 2f) - (this.getHitboxLocator().getParentBone().getScale().y - 1) / 3f;
        float newHitboxZ = -(float) modelSpacePos.z / 16f;

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
