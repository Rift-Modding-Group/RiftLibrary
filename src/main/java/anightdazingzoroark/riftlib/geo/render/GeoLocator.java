package anightdazingzoroark.riftlib.geo.render;

import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Quaternion;

public class GeoLocator {
    public final GeoBone parent;
    public final String name;
    private float positionX;
    private float positionY;
    private float positionZ;
    private float rotationX;
    private float rotationY;
    private float rotationZ;
    private float rotationPointX;
    private float rotationPointY;
    private float rotationPointZ;
    private RiftLibParticleEmitter particleEmitter;

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

    public float getRotationPointX() {
        return this.rotationPointX;
    }

    public float getRotationPointY() {
        return this.rotationPointY;
    }

    public float getRotationPointZ() {
        return this.rotationPointZ;
    }

    public void setRotationPointX(float value) {
        this.rotationPointX = value;
    }

    public void setRotationPointY(float value) {
        this.rotationPointY = value;
    }

    public void setRotationPointZ(float value) {
        this.rotationPointZ = value;
    }

    public void createParticleEmitter(ParticleBuilder builder) {
        this.particleEmitter = new RiftLibParticleEmitter(builder, Minecraft.getMinecraft().world, this);
    }

    public RiftLibParticleEmitter getParticleEmitter() {
        return this.particleEmitter;
    }

    public Vec3d getUnoffsettedPosition() {
        return new Vec3d(this.positionX, this.positionY, this.positionZ);
    }

    public Vec3d getPosition() {
        Vec3d boneDispOffset = this.getPositionOffsetFromBoneDisplacements();
        Vec3d boneRotOffset = this.getPositionOffsetFromBoneRotations();

        return new Vec3d(
                (this.positionX / 16f) + boneDispOffset.x + boneRotOffset.x,
                (this.positionY / 16f) + boneDispOffset.y + boneRotOffset.y,
                (this.positionZ / 16f) + boneDispOffset.z + boneRotOffset.z
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
        Vec3d vecPos = new Vec3d(this.positionX / 16f, this.positionY / 16f, this.positionZ / 16f);
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

        return vecPos.subtract(this.positionX / 16f, this.positionY / 16f, this.positionZ / 16f);
    }

    private Vec3d getRotationOffsetFromBoneRotations() {
        Vec3d toReturn = Vec3d.ZERO;
        GeoBone boneToTest = this.parent;

        while (boneToTest != null) {
            toReturn = toReturn.add(boneToTest.getRotationX(), -boneToTest.getRotationY(), -boneToTest.getRotationZ());
            boneToTest = boneToTest.parent;
        }
        return toReturn;
    }

    public String toString() {
        return "[name="+this.name+", offset=("+this.positionX+", "+this.positionY+", "+this.positionZ+")]";
    }
}
