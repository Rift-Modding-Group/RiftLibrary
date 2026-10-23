package anightdazingzoroark.riftlib.model;

import anightdazingzoroark.riftlib.geo.GeoBone;
import anightdazingzoroark.riftlib.geo.GeoBoundingBox;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.AxisAlignedBB;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.util.MatrixStack;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.util.Map;
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
     * Returns the model-space attachment point used by hitboxes.
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

    /** Full posed bounds, including all ancestor transforms and rotated extents. */
    @NotNull
    public AxisAlignedBB getWorldSpaceBounds(float scale, float yaw, @NotNull Vec3d position, @NotNull Map<String, BoneSnapshot> poses) {
        MatrixStack matrices = new MatrixStack();
        matrices.rotateY((float) (Math.PI - Math.toRadians(yaw)));
        matrices.scale(scale, scale, scale);
        List<GeoBone> chain = new ArrayList<>();
        for (GeoBone bone = this.boundingBox.parent; bone != null; bone = bone.parent) chain.add(bone);
        for (GeoBone bone : chain.reversed()) {
            BoneSnapshot pose = poses.get(bone.getName());
            Vector3f displacement = bone.getPosition();
            Vector3f rotation = bone.getRotation();
            Vector3f boneScale = bone.getScale();
            if (pose != null) {
                displacement = pose.getPosition();
                rotation = pose.getRotation();
                boneScale = pose.getScale();
            }
            matrices.translate(-displacement.x / 16f, displacement.y / 16f, displacement.z / 16f);
            matrices.moveToPivot(bone);
            matrices.rotateZ(rotation.z);
            matrices.rotateY(rotation.y);
            matrices.rotateX(rotation.x);
            matrices.scale(boneScale.x, boneScale.y, boneScale.z);
            matrices.moveBackFromPivot(bone);
        }
        float[] size = this.getUnscaledModelSpaceSize();
        Vector3f origin = this.boundingBox.getPosition();
        Point3f minimum = null;
        Point3f maximum = null;
        for (int corner = 0; corner < 8; corner++) {
            Point3f point = new Point3f(
                    (origin.x - size[0] + ((corner & 1) == 0 ? 0f : size[0])) / 16f,
                    (origin.y + ((corner & 2) == 0 ? 0f : size[1])) / 16f,
                    (origin.z + ((corner & 4) == 0 ? 0f : size[0])) / 16f
            );
            matrices.getModelMatrix().transform(point);
            if (minimum == null) {
                minimum = new Point3f(point);
                maximum = new Point3f(point);
            }
            else {
                minimum.set(Math.min(minimum.x, point.x), Math.min(minimum.y, point.y), Math.min(minimum.z, point.z));
                maximum.set(Math.max(maximum.x, point.x), Math.max(maximum.y, point.y), Math.max(maximum.z, point.z));
            }
        }
        return new AxisAlignedBB(position.x + minimum.x, position.y + minimum.y, position.z + minimum.z,
                position.x + maximum.x, position.y + maximum.y, position.z + maximum.z);
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
        float[] size = this.getUnscaledModelSpaceSize();
        return new Vec3d(
                -this.boundingBox.getPosition().x + size[0] / 2D,
                this.boundingBox.getPosition().y + size[1] / 2D,
                this.boundingBox.getPosition().z + size[0] / 2D
        );
    }
}
