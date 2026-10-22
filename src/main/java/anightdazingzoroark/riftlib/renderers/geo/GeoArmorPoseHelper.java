package anightdazingzoroark.riftlib.renderers.geo;

import anightdazingzoroark.riftlib.geo.GeoBone;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.util.MatrixStack;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeoArmorPoseHelper {
    /**
     * Maps the current geometry pose into vanilla's downward-Y model space.
     * */
    public static void copyToBiped(@NotNull IGeoArmorPoseProvider provider, @NotNull EntityLivingBase wearer, @NotNull ModelBiped armor) {
        for (GeoArmorRenderer.ArmorBodyPart part : GeoArmorRenderer.ArmorBodyPart.values()) {
            GeoBone source = provider.getArmorPoseBone(wearer, part);
            if (source == null) continue;
            copyBoneToBiped(source, part.getBipedBone(armor));
        }
        ModelBase.copyModelAngles(armor.bipedHead, armor.bipedHeadwear);
    }

    public static void copyBoneToBiped(@NotNull GeoBone source, @NotNull ModelRenderer target) {
        Matrix4f transform = getBoneTransform(source);
        Point3f pivot = new Point3f(source.getPivot());
        pivot.scale(1f / 16f);
        transform.transform(pivot);
        target.setRotationPoint(-pivot.x * 16f, (1.501f - pivot.y) * 16f, pivot.z * 16f);

        // Conjugate by vanilla's (-1, -1, 1) axis conversion.
        transform.m02 *= -1f;
        transform.m12 *= -1f;
        transform.m20 *= -1f;
        transform.m21 *= -1f;
        Vector3f rotation = getRotation(transform);
        target.rotateAngleX = rotation.x;
        target.rotateAngleY = rotation.y;
        target.rotateAngleZ = rotation.z;
    }

    /**
     * Complete deformation, including translated/rotated/scaled ancestors.
     * */
    @NotNull
    public static Matrix4f getBoneTransform(@NotNull GeoBone bone) {
        List<GeoBone> ancestors = new ArrayList<>();
        for (GeoBone current = bone; current != null; current = current.parent) ancestors.add(current);
        MatrixStack matrices = new MatrixStack();
        for (GeoBone current : ancestors.reversed()) {
            matrices.translate(current);
            matrices.moveToPivot(current);
            matrices.rotate(current);
            matrices.scale(current);
            matrices.moveBackFromPivot(current);
        }
        return new Matrix4f(matrices.getModelMatrix());
    }

    /**
     * Applies absolute source poses without applying a bound parent twice.
     * */
    public static void applyToGeometry(@NotNull GeoModel model, @NotNull Map<GeoBone, Matrix4f> transforms) {
        Matrix4f identity = new Matrix4f();
        identity.setIdentity();
        for (GeoBone root : model.getTopLevelBones()) applyToBone(root, identity, transforms);
    }

    private static void applyToBone(@NotNull GeoBone bone, @NotNull Matrix4f parent, @NotNull Map<GeoBone, Matrix4f> transforms) {
        Matrix4f desired = transforms.get(bone);
        if (desired != null && Math.abs(parent.determinant()) > 0.000001f) {
            Matrix4f local = new Matrix4f(parent);
            local.invert();
            local.mul(desired);
            Vector3f rotation = getRotation(local);
            bone.getRotation().set(rotation);
            bone.getScale().set(getScale(local));
            Vector3f pivot = new Vector3f(bone.getPivot());
            pivot.scale(1f / 16f);
            Vector3f rotatedPivot = new Vector3f(pivot);
            local.transform(rotatedPivot);
            bone.getPosition().set(
                    -(local.m03 - pivot.x + rotatedPivot.x) * 16f,
                    (local.m13 - pivot.y + rotatedPivot.y) * 16f,
                    (local.m23 - pivot.z + rotatedPivot.z) * 16f
            );
        }

        MatrixStack localPose = new MatrixStack();
        localPose.translate(bone);
        localPose.moveToPivot(bone);
        localPose.rotate(bone);
        localPose.scale(bone);
        localPose.moveBackFromPivot(bone);
        Matrix4f world = new Matrix4f(parent);
        world.mul(localPose.getModelMatrix());
        for (GeoBone child : bone.childBones) applyToBone(child, world, transforms);
    }

    @NotNull
    private static Vector3f getScale(@NotNull Matrix4f matrix) {
        return new Vector3f(
                (float) Math.sqrt(matrix.m00 * matrix.m00 + matrix.m10 * matrix.m10 + matrix.m20 * matrix.m20),
                (float) Math.sqrt(matrix.m01 * matrix.m01 + matrix.m11 * matrix.m11 + matrix.m21 * matrix.m21),
                (float) Math.sqrt(matrix.m02 * matrix.m02 + matrix.m12 * matrix.m12 + matrix.m22 * matrix.m22)
        );
    }

    @NotNull
    private static Vector3f getRotation(@NotNull Matrix4f matrix) {
        Vector3f scale = getScale(matrix);
        if (scale.x < 0.000001f || scale.y < 0.000001f || scale.z < 0.000001f) return new Vector3f();
        float y = (float) Math.asin(Math.clamp(-matrix.m20 / scale.x, -1f, 1f));
        float x;
        float z;
        if (Math.abs(Math.cos(y)) > 0.000001D) {
            x = (float) Math.atan2(matrix.m21 / scale.y, matrix.m22 / scale.z);
            z = (float) Math.atan2(matrix.m10 / scale.x, matrix.m00 / scale.x);
        }
        else {
            x = (float) Math.atan2(-matrix.m12 / scale.z, matrix.m11 / scale.y);
            z = 0f;
        }
        return new Vector3f(x, y, z);
    }
}
