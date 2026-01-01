package anightdazingzoroark.riftlib.geo.render;

import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector4f;

import java.util.ArrayList;

public class GeoLocator {
    public final GeoBone parent;
    public final String name;
    private final float positionX;
    private final float positionY;
    private final float positionZ;
    private final float rotationX;
    private final float rotationY;
    private final float rotationZ;

    public GeoLocator(GeoBone parent, String name, float x, float y, float z, float rotationX, float rotationY, float rotationZ) {
        this.parent = parent;
        this.name = name;
        this.positionX = x;
        this.positionY = y;
        this.positionZ = z;
        this.rotationX = (float) Math.toRadians(rotationX);
        this.rotationY = (float) Math.toRadians(rotationY);
        this.rotationZ = (float) Math.toRadians(rotationZ);
    }

    public Vec3d getPosition() {
        Vec3d boneDispOffset = this.getPositionOffsetFromBoneDisplacements();
        Vec3d boneRotOffset = this.getPositionOffsetFromBoneRotations();

        return new Vec3d(
                this.positionX + boneDispOffset.x + boneRotOffset.x,
                this.positionY + boneDispOffset.y + boneRotOffset.y,
                this.positionZ + boneDispOffset.z + boneRotOffset.z
        );
    }

    //summing up rotations sucks ass overall so this is to be used instead
    //when it comes to dealing with rotations
    //todo: add parameters for additional angles
    public Quaternion getXYZQuaternion() {
        Quaternion toReturn = new Quaternion(0, 0, 0, 1);

        //setup for getting from ancestor -> from child
        ArrayList<GeoBone> chain = new ArrayList<>();
        for (GeoBone boneToTest = this.parent; boneToTest != null; boneToTest = boneToTest.parent) chain.add(boneToTest);

        //multiply rotation quaternions in each chain
        for (int i = chain.size() - 1; i >= 0; i--) {
            GeoBone boneToTest = chain.get(i);
            double cosX = Math.cos(boneToTest.getRotationX() / 2);
            double sinX = Math.sin(boneToTest.getRotationX() / 2);
            //note to self: negating y rotation is more or less a weird hack, idk if this is really necessary
            double cosY = Math.cos(-boneToTest.getRotationY() / 2);
            double sinY = Math.sin(-boneToTest.getRotationY() / 2);
            double cosZ = Math.cos(-boneToTest.getRotationZ() / 2);
            double sinZ = Math.sin(-boneToTest.getRotationZ() / 2);

            Quaternion quatBone = new Quaternion(
                    (float) (sinX * cosY * cosZ - cosX * sinY * sinZ),
                    (float) (cosX * sinY * cosZ + sinX * cosY * sinZ),
                    (float) (cosX * cosY * sinZ - sinX * sinY * cosZ),
                    (float) (cosX * cosY * cosZ + sinX * sinY * sinZ)
            );

            Quaternion.normalise(quatBone, quatBone);

            Quaternion tmp = new Quaternion();
            Quaternion.mul(toReturn, quatBone, tmp);
            toReturn.set(tmp);
        }

        //now apply the locator's rotation
        double cosX = Math.cos(this.rotationX / 2);
        double sinX = Math.sin(this.rotationX / 2);
        double cosY = Math.cos(this.rotationY / 2);
        double sinY = Math.sin(this.rotationY / 2);
        double cosZ = Math.cos(-this.rotationZ / 2);
        double sinZ = Math.sin(-this.rotationZ / 2);

        Quaternion quatLocator = new Quaternion(
                (float) (sinX * cosY * cosZ - cosX * sinY * sinZ),
                (float) (cosX * sinY * cosZ + sinX * cosY * sinZ),
                (float) (cosX * cosY * sinZ - sinX * sinY * cosZ),
                (float) (cosX * cosY * cosZ + sinX * sinY * sinZ)
        );

        Quaternion.normalise(quatLocator, quatLocator);

        Quaternion tmp = new Quaternion();
        Quaternion.mul(toReturn, quatLocator, tmp);
        toReturn.set(tmp);

        //normalize and return
        Quaternion.normalise(toReturn, toReturn);

        return toReturn;
    }

    private Vec3d getPositionOffsetFromBoneDisplacements() {
        Vec3d toReturn = Vec3d.ZERO;
        GeoBone boneToTest = this.parent;

        while (boneToTest != null) {
            toReturn = toReturn.add(boneToTest.getPositionX() / 16f, boneToTest.getPositionY() / 16f, boneToTest.getPositionZ() / 16f);
            boneToTest = boneToTest.parent;
        }
        return toReturn;
    }

    private Vec3d getPositionOffsetFromBoneRotations() {
        Vec3d vecPos = new Vec3d(this.positionX, this.positionY, this.positionZ);
        GeoBone boneToTest = this.parent;

        //evaluate
        while (boneToTest != null) {
            //get vector for direction from pivot to pos
            Vec3d vecPivot = new Vec3d(boneToTest.getPivotX() / 16D, boneToTest.getPivotY() / 16D, boneToTest.getPivotZ() / 16D);
            Vec3d vecDirection = vecPos.subtract(vecPivot);

            //create quaternion from current rotations, conjugate it too
            double cosX = Math.cos(boneToTest.getRotationX() / 2);
            double sinX = Math.sin(boneToTest.getRotationX() / 2);
            //note to self: negating y rotation is more or less a weird hack, idk if this is really necessary
            double cosY = Math.cos(-boneToTest.getRotationY() / 2);
            double sinY = Math.sin(-boneToTest.getRotationY() / 2);
            double cosZ = Math.cos(-boneToTest.getRotationZ() / 2);
            double sinZ = Math.sin(-boneToTest.getRotationZ() / 2);

            Quaternion quatBoneRot = new Quaternion(
                    (float) (sinX * cosY * cosZ - cosX * sinY * sinZ),
                    (float) (cosX * sinY * cosZ + sinX * cosY * sinZ),
                    (float) (cosX * cosY * sinZ - sinX * sinY * cosZ),
                    (float) (cosX * cosY * cosZ + sinX * sinY * sinZ)
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
            boneToTest = boneToTest.parent;
        }

        return vecPos.subtract(this.positionX, this.positionY, this.positionZ);
    }

    public String toString() {
        return "[name="+this.name+", offset=("+this.positionX+", "+this.positionY+", "+this.positionZ+")]";
    }
}
