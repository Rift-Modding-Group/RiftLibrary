package anightdazingzoroark.riftlibrary.main.geo.basic;

import anightdazingzoroark.riftlibrary.main.util.QuaternionUtils;
import anightdazingzoroark.riftlibrary.main.util.VectorUtils;
import org.lwjglx.util.vector.Quaternion;
import org.lwjglx.util.vector.Vector3f;

import java.util.ArrayList;

public class RiftLibLocator {
    public final RiftLibBone parent;
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

    public RiftLibLocator(RiftLibBone parent, String name) {
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

    public Vector3f getPosition() {
        Vector3f boneDispOffset = this.getPositionOffsetFromBoneDisplacements();
        Vector3f boneRotOffset = this.getPositionOffsetFromBoneRotations();

        return new Vector3f(
                this.positionX + boneDispOffset.x + boneRotOffset.x,
                this.positionY + boneDispOffset.y + boneRotOffset.y,
                this.positionZ + boneDispOffset.z + boneRotOffset.z
        );
    }

    //summing up rotations sucks ass overall so this is to be used instead
    //when it comes to dealing with rotations
    public Quaternion getYXZQuaternion() {
        Quaternion toReturn = new Quaternion(0, 0, 0, 1);

        //setup for getting from ancestor -> from child
        ArrayList<RiftLibBone> chain = new ArrayList<>();
        for (RiftLibBone boneToTest = this.parent; boneToTest != null; boneToTest = boneToTest.parent) chain.add(boneToTest);

        //multiply rotation quaternions in each chain
        for (int i = chain.size() - 1; i >= 0; i--) {
            RiftLibBone boneToTest = chain.get(i);
            Quaternion quatBone = QuaternionUtils.createYXZQuaternion(
                    boneToTest.getRotationX(),
                    boneToTest.getRotationY(),
                    -boneToTest.getRotationZ()
            );

            Quaternion.normalise(quatBone, quatBone);

            Quaternion tmp = new Quaternion();
            Quaternion.mul(toReturn, quatBone, tmp);
            toReturn.set(tmp);
        }

        //now apply the locator's rotation
        Quaternion quatLocator = QuaternionUtils.createYXZQuaternion(
                this.rotationX,
                this.rotationY,
                -this.rotationZ
        );

        Quaternion.normalise(quatLocator, quatLocator);

        Quaternion tmp = new Quaternion();
        Quaternion.mul(toReturn, quatLocator, tmp);
        toReturn.set(tmp);

        //normalize and return
        Quaternion.normalise(toReturn, toReturn);

        return toReturn;
    }

    private Vector3f getPositionOffsetFromBoneDisplacements() {
        Vector3f toReturn = new Vector3f();
        RiftLibBone boneToTest = this.parent;

        while (boneToTest != null) {
            Vector3f.add(
                    toReturn,
                    new Vector3f(boneToTest.getPositionX(), boneToTest.getPositionY(), boneToTest.getPositionZ()),
                    toReturn
            );
            boneToTest = boneToTest.parent;
        }
        return toReturn;
    }

    private Vector3f getPositionOffsetFromBoneRotations() {
        Vector3f vecPos = new Vector3f(this.positionX, this.positionY, this.positionZ);
        RiftLibBone boneToTest = this.parent;

        //evaluate
        while (boneToTest != null) {
            //get vector for direction from pivot to pos
            Vector3f vecPivot = new Vector3f(boneToTest.getPivotX(), boneToTest.getPivotY(), boneToTest.getPivotZ());
            Vector3f vecDirection = new Vector3f();
            Vector3f.sub(vecPos, vecPivot, vecDirection);

            //create quaternion from current rotations, conjugate it too
            Quaternion quatBoneRot = QuaternionUtils.createXYZQuaternion(
                    boneToTest.getRotationX(),
                    boneToTest.getRotationY(),
                    -boneToTest.getRotationZ()
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
            Vector3f rotatedVecDirection = QuaternionUtils.convertQuaternionToVec(quatRotatedVecDirection);

            //update vecPos
            Vector3f.sub(rotatedVecDirection, vecPivot, vecPos);
            boneToTest = boneToTest.parent;
        }

        Vector3f toReturn = new Vector3f();
        Vector3f.sub(
                vecPos,
                new Vector3f(this.positionX, this.positionY, this.positionZ),
                toReturn
        );
        return toReturn;
    }

    public String toString() {
        return "[name="+this.name+", offset=("+this.positionX+", "+this.positionY+", "+this.positionZ+")]";
    }
}
