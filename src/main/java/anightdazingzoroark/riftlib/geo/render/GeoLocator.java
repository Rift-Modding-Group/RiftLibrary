package anightdazingzoroark.riftlib.geo.render;

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
        return new Vec3d(
                this.positionX + this.getPositionOffsetFromBoneDisplacements().x + this.getPositionOffsetFromBoneRotations().x,
                this.positionY + this.getPositionOffsetFromBoneDisplacements().y + this.getPositionOffsetFromBoneRotations().y,
                this.positionZ + this.getPositionOffsetFromBoneDisplacements().z + this.getPositionOffsetFromBoneRotations().z
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

    //todo: change summing of bone data so that its parent -> child
    private Vec3d getPositionOffsetFromBoneDisplacements() {
        Vec3d toReturn = Vec3d.ZERO;
        GeoBone boneToTest = this.parent;

        while (boneToTest != null) {
            toReturn = toReturn.add(boneToTest.getPositionX() / 16f, boneToTest.getPositionY() / 16f, boneToTest.getPositionZ() / 16f);
            boneToTest = boneToTest.parent;
        }
        return toReturn;
    }

    //todo: change summing of bone data so that its parent -> child
    private Vec3d getPositionOffsetFromBoneRotations() {
        Vec3d toReturn = new Vec3d(this.positionX, this.positionY, this.positionZ);
        GeoBone boneToTest = this.parent;

        while (boneToTest != null) {
            double pivotX = boneToTest.getPivotX() / 16D;
            double pivotY = boneToTest.getPivotY() / 16D;
            double pivotZ = boneToTest.getPivotZ() / 16D;

            double relX = toReturn.x - pivotX;
            double relY = toReturn.y - pivotY;
            double relZ = toReturn.z - pivotZ;

            //create offsets from x rotation, which affects y and z offsets
            double cosX = Math.cos(boneToTest.getRotationX());
            double sinX = Math.sin(boneToTest.getRotationX());
            double ry = relY * cosX - relZ * sinX;
            double rz = relY * sinX + relZ * cosX;
            relY = ry;
            relZ = rz;

            //create offsets from y rotation, which affects x and z offsets
            double cosY = Math.cos(boneToTest.getRotationY());
            double sinY = Math.sin(boneToTest.getRotationY());
            double rx = relX * cosY - relZ * sinY;
            rz = relX * sinY + relZ * cosY;
            relX = rx;
            relZ = rz;

            //create offsets from z rotation, which affects x and y offsets
            double cosZ = Math.cos(-boneToTest.getRotationZ());
            double sinZ = Math.sin(-boneToTest.getRotationZ());
            rx = relX * cosZ - relY * sinZ;
            ry = relX * sinZ + relY * cosZ;
            relX = rx;
            relY = ry;

            toReturn = new Vec3d(relX + pivotX, relY + pivotY, relZ + pivotZ);
            boneToTest = boneToTest.parent;
        }

        return toReturn.subtract(this.positionX, this.positionY, this.positionZ);
    }

    public String toString() {
        return "[name="+this.name+", offset=("+this.positionX+", "+this.positionY+", "+this.positionZ+")]";
    }
}
