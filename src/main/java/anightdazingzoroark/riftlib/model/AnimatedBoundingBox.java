package anightdazingzoroark.riftlib.model;

import anightdazingzoroark.riftlib.core.ExpressionValue;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.geo.GeoBone;
import anightdazingzoroark.riftlib.geo.GeoBoundingBox;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.lwjglx.util.vector.Quaternion;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for GeoBoundingBox instances for transformations
 * and world space positions
 * */
public class AnimatedBoundingBox {
    @NotNull
    private final GeoBoundingBox boundingBox;

    public AnimatedBoundingBox(@NonNull GeoBoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    @NotNull
    public String getName() {
        return this.boundingBox.name;
    }

    public boolean canDoCollisions() {
        return this.boundingBox.canCollide;
    }

    @NotNull
    public String[] getTags() {
        return this.boundingBox.tags;
    }

    public ExpressionValue getDamageMultiplier() {
        return this.boundingBox.damageMultiplier;
    }

    public float[] getModelSpaceSize() {
        float parentHorizontalScale = Math.max(this.boundingBox.parent.getScale().x, this.boundingBox.parent.getScale().z);
        float parentVerticalScale = this.boundingBox.parent.getScale().y;

        return new float[]{
                this.boundingBox.getSize()[0] * parentHorizontalScale,
                this.boundingBox.getSize()[1] * parentVerticalScale
        };
    }

    public float[] getUnscaledModelSpaceSize() {
        return this.boundingBox.getSize();
    }

    /**
     * Note that this isn't the centerpoint of
     * */
    @NotNull
    public Vec3d getModelSpacePosition() {
        Vec3d boneDispOffset = this.getPositionOffsetFromBoneDisplacements();
        Vec3d boneRotOffset = this.getPositionOffsetFromBoneRotations();
        Vec3d locatorPos = this.getBoundingBoxPosition();

        return new Vec3d(
                locatorPos.x + boneDispOffset.x + boneRotOffset.x,
                locatorPos.y + boneDispOffset.y + boneRotOffset.y,
                locatorPos.z + boneDispOffset.z + boneRotOffset.z
        );
    }

    private Vec3d getPositionOffsetFromBoneDisplacements() {
        Vec3d toReturn = Vec3d.ZERO;
        GeoBone boneToTest = this.boundingBox.parent;

        while (boneToTest != null) {
            toReturn = toReturn.add(boneToTest.getPosition().x, boneToTest.getPosition().y, boneToTest.getPosition().z);
            boneToTest = boneToTest.parent;
        }
        return toReturn;
    }

    private Vec3d getPositionOffsetFromBoneRotations() {
        Vec3d locatorPos = this.getBoundingBoxPosition();
        Vec3d vecPos = locatorPos;

        List<GeoBone> chain = new ArrayList<>();
        for (GeoBone boneToTest = this.boundingBox.parent; boneToTest != null; boneToTest = boneToTest.parent) chain.add(boneToTest);

        //evaluate from child to parent. MatrixStack multiplies parent transforms before
        //child transforms, so the locator point is affected by child transforms first.
        for (GeoBone boneToTest : chain) {
            //get vector for direction from pivot to pos
            Vec3d vecPivot = new Vec3d(
                    boneToTest.getPivot().x,
                    boneToTest.getPivot().y,
                    boneToTest.getPivot().z
            );
            Vec3d vecDirection = vecPos.subtract(vecPivot);

            //create quaternion from current rotations, conjugate it too
            Quaternion quatBoneRot = QuaternionUtils.createXYZQuaternion(
                    boneToTest.getRotation().x,
                    boneToTest.getRotation().y,
                    boneToTest.getRotation().z
            );
            Quaternion.normalise(quatBoneRot, quatBoneRot);

            Quaternion quatBoneRotConj = new Quaternion();
            Quaternion.negate(quatBoneRot, quatBoneRotConj);

            //rotate vecDirection
            Quaternion quatVecDirection = VectorUtils.convertToQuaternion(vecDirection);
            Quaternion temp = new Quaternion();
            Quaternion quatRotatedVecDirection = new Quaternion();
            Quaternion.mul(quatBoneRot, quatVecDirection, temp);
            Quaternion.mul(temp, quatBoneRotConj, quatRotatedVecDirection);
            Vec3d rotatedVecDirection = VectorUtils.convertQuaternionToVec(quatRotatedVecDirection);

            //update vecPos
            vecPos = rotatedVecDirection.add(vecPivot);
        }

        return vecPos.subtract(locatorPos);
    }

    private Vec3d getBoundingBoxPosition() {
        return new Vec3d(
                this.boundingBox.getPosition().x,
                this.boundingBox.getPosition().y,
                this.boundingBox.getPosition().z
        );
    }
}
