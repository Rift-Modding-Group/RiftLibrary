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

    //todo: change hitbox and dynamic ride positions to use this eventually (?)
    public Quaternion computeQuaternion() {
        Quaternion toReturn = new Quaternion();

        //setup for getting from ancestor -> from child
        ArrayList<GeoBone> chain = new ArrayList<>();
        for (GeoBone boneToTest = this.parent; boneToTest != null; boneToTest = boneToTest.parent) chain.add(boneToTest);

        //get info from ancestor to child
        for (int i = chain.size() - 1; i >= 0; i--) {
            GeoBone boneToTest = chain.get(i);
            Quaternion quatBone = quatFromEulerXYZ(boneToTest.getRotationX(), -boneToTest.getRotationY(), -boneToTest.getRotationZ());
            Quaternion.mul(toReturn, quatBone, toReturn);
            Quaternion.normalise(toReturn, toReturn);
        }

        Quaternion quatLoc = this.quatFromEulerXYZ(this.rotationX, this.rotationY, -this.rotationZ);
        Quaternion.mul(toReturn, quatLoc, toReturn);
        Quaternion.normalise(toReturn, toReturn);

        return toReturn;
    }

    private Quaternion quatFromEulerXYZ(float rx, float ry, float rz) {
        Quaternion quatX = new Quaternion();
        quatX.setFromAxisAngle(new Vector4f(1, 0, 0, rx));

        Quaternion quatY = new Quaternion();
        quatY.setFromAxisAngle(new Vector4f(0, 1, 0, ry));

        Quaternion quatZ = new Quaternion();
        quatZ.setFromAxisAngle(new Vector4f(0, 0, 1, rz));

        Quaternion toReturn = new Quaternion();
        Quaternion.mul(quatZ, quatY, toReturn);
        Quaternion.mul(toReturn, quatX, toReturn);
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

    private Vec3d getRotationOffsetFromBoneRotations() {
        Vec3d toReturn = Vec3d.ZERO;
        GeoBone boneToTest = this.parent;

        while (boneToTest != null) {
            toReturn = toReturn.add(boneToTest.getRotationX(), boneToTest.getRotationY(), boneToTest.getRotationZ());
            boneToTest = boneToTest.parent;
        }
        return toReturn;
    }

    public String toString() {
        return "[name="+this.name+", offset=("+this.positionX+", "+this.positionY+", "+this.positionZ+")]";
    }
}
