package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.Entity;
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
    public T getParent();

    /**
     * Return the bounding box this hitbox is based on
     * */
    @NotNull
    public AnimatedBoundingBox getBoundingBox();

    /**
     * Return the width and height based on parent scale and fixed size
     * */
    public default float[] getHitboxSize() {
        float parentScale = this.getParent().getMultiHitboxUser().getAnimationData().getScale();
        return new float[]{
                parentScale * this.getBoundingBox().getModelSpaceSize()[0] / 16f,
                parentScale * this.getBoundingBox().getModelSpaceSize()[1] / 16f
        };
    }

    /**
     * Return the position of the hitbox based on parent position and yaw
     * and locator position
     * */
    @NotNull
    public default Vec3d getHitboxPosition() {
        Entity parentEntity = this.getParent().getMultiHitboxUser();
        float parentScale = this.getParent().getMultiHitboxUser().getAnimationData().getScale();

        //correct the model space positions first
        Vec3d modelSpacePos = this.getBoundingBox().getModelSpacePosition();
        float[] modelSpaceSize = this.getBoundingBox().getModelSpaceSize();

        float newHitboxX = (float) (-modelSpacePos.x / 16f);
        float newHitboxY = (float) ((modelSpacePos.y - modelSpaceSize[1] / 2f) / 16f);
        float newHitboxZ = (float) (-modelSpacePos.z / 16f);

        //set initial entity offset from center
        Vec3d posVec = new Vec3d(newHitboxX * parentScale, newHitboxY * parentScale, newHitboxZ * parentScale);

        //determine yaw
        double finalYawRadians = -Math.toRadians(this.getParent().getMultiHitboxUser().getAnimationData().getModelYaw());

        //rotate vector around yaw
        Quaternion quaternion = QuaternionUtils.createXYZQuaternion(0, finalYawRadians, 0);
        posVec = VectorUtils.rotateVectorWithQuaternion(posVec, quaternion);

        //put in world
        return new Vec3d(
                parentEntity.posX + posVec.x,
                parentEntity.posY + posVec.y,
                parentEntity.posZ + posVec.z
        );
    }
}
