package anightdazingzoroark.riftlib.geo.render;

import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Quaternion;

import java.util.ArrayList;

//todo: add back animatedlocator it seems because 2 declarations of
//geolocator appears to fuck over simultaneous particle rendering
public class GeoLocator {
    public final GeoBone parent;
    public final String name;
    private float positionX;
    private float positionY;
    private float positionZ;
    private float rotationX;
    private float rotationY;
    private float rotationZ;
    private float pivotX;
    private float pivotY;
    private float pivotZ;

    public GeoLocator(GeoBone parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public float getPositionX() {
        return this.positionX;
    }

    public float getPositionY() {
        return this.positionY;
    }

    public float getPositionZ() {
        return this.positionZ;
    }

    public void setPositionX(float value) {
        this.positionX = value;
    }

    public void setPositionY(float value) {
        this.positionY = value;
    }

    public void setPositionZ(float value) {
        this.positionZ = value;
    }

    public float getRotationX() {
        return this.rotationX;
    }

    public float getRotationY() {
        return this.rotationY;
    }

    public float getRotationZ() {
        return this.rotationZ;
    }

    public void setRotationX(float value) {
        this.rotationX = value;
    }

    public void setRotationY(float value) {
        this.rotationY = value;
    }

    public void setRotationZ(float value) {
        this.rotationZ = value;
    }

    public float getPivotX() {
        return this.pivotX;
    }

    public float getPivotY() {
        return this.pivotY;
    }

    public float getPivotZ() {
        return this.pivotZ;
    }

    public void setPivotX(float value) {
        this.pivotX = value;
    }

    public void setPivotY(float value) {
        this.pivotY = value;
    }

    public void setPivotZ(float value) {
        this.pivotZ = value;
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

    public Vec3d getRotation() {
        Vec3d boneRotOffset = this.getRotationOffsetFromBoneRotations();
        return new Vec3d(
                this.rotationX + boneRotOffset.x,
                this.rotationY + boneRotOffset.y,
                this.rotationZ + boneRotOffset.z
        );
    }

    //summing up rotations sucks ass overall so this is to be used instead
    //when it comes to dealing with rotations
    public Quaternion getYXZQuaternion() {
        Quaternion toReturn = new Quaternion(0, 0, 0, 1);

        //setup for getting from ancestor -> from child
        ArrayList<GeoBone> chain = new ArrayList<>();
        for (GeoBone boneToTest = this.parent; boneToTest != null; boneToTest = boneToTest.parent) chain.add(boneToTest);

        //multiply rotation quaternions in each chain
        for (int i = chain.size() - 1; i >= 0; i--) {
            GeoBone boneToTest = chain.get(i);
            double cosY = Math.cos(boneToTest.getRotationY() / 2);
            double sinY = Math.sin(boneToTest.getRotationY() / 2);

            double cosX = Math.cos(boneToTest.getRotationX() / 2);
            double sinX = Math.sin(boneToTest.getRotationX() / 2);

            double cosZ = Math.cos(-boneToTest.getRotationZ() / 2);
            double sinZ = Math.sin(-boneToTest.getRotationZ() / 2);

            Quaternion quatBone = new Quaternion(
                    (float) (sinX * cosY * cosZ + cosX * sinY * sinZ),
                    (float) (cosX * sinY * cosZ - sinX * cosY * sinZ),
                    (float) (cosX * cosY * sinZ + sinX * sinY * cosZ),
                    (float) (cosX * cosY * cosZ - sinX * sinY * sinZ)
            );

            Quaternion.normalise(quatBone, quatBone);

            Quaternion tmp = new Quaternion();
            Quaternion.mul(toReturn, quatBone, tmp);
            toReturn.set(tmp);
        }

        //now apply the locator's rotation
        double cosY = Math.cos(-this.rotationY / 2);
        double sinY = Math.sin(-this.rotationY / 2);
        double cosX = Math.cos(-this.rotationX / 2);
        double sinX = Math.sin(-this.rotationX / 2);
        double cosZ = Math.cos(-this.rotationZ / 2);
        double sinZ = Math.sin(-this.rotationZ / 2);

        Quaternion quatLocator = new Quaternion(
                (float) (sinX * cosY * cosZ + cosX * sinY * sinZ),
                (float) (cosX * sinY * cosZ - sinX * cosY * sinZ),
                (float) (cosX * cosY * sinZ + sinX * sinY * cosZ),
                (float) (cosX * cosY * cosZ - sinX * sinY * sinZ)
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
            toReturn = toReturn.add(boneToTest.getPositionX(), boneToTest.getPositionY(), boneToTest.getPositionZ());
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
            Vec3d vecPivot = new Vec3d(boneToTest.getPivotX(), boneToTest.getPivotY(), boneToTest.getPivotZ());
            Vec3d vecDirection = vecPos.subtract(vecPivot);

            //create quaternion from current rotations, conjugate it too
            double cosX = Math.cos(boneToTest.getRotationX() / 2);
            double sinX = Math.sin(boneToTest.getRotationX() / 2);
            double cosY = Math.cos(boneToTest.getRotationY() / 2);
            double sinY = Math.sin(boneToTest.getRotationY() / 2);
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
