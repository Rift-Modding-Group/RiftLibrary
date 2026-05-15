package anightdazingzoroark.riftlib.model;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.core.manager.AnimationDataTileEntity;
import anightdazingzoroark.riftlib.geo.render.GeoBone;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleTicker;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import org.lwjglx.util.vector.Quaternion;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a wrapper for GeoLocator that allows for things
 * such as quaternions or world space positions.
 * */
public class AnimatedLocator {
    private final GeoLocator locator;
    private final AbstractAnimationData<?> animationData;
    private Vec3d worldSpacePos = new Vec3d(0, 0, 0);
    private Quaternion worldSpaceYXZQuaternion = new Quaternion();
    private boolean isUpdated = true;

    public AnimatedLocator(GeoLocator geoLocator, AbstractAnimationData<?> animationData) {
        this.locator = geoLocator;
        this.animationData = animationData;
    }

    public void setUpdated(boolean value) {
        this.isUpdated = value;
    }

    public boolean isUpdated() {
        return this.isUpdated;
    }

    public boolean isValid() {
        return this.animationData.isValid();
    }

    public String getName() {
        return this.locator.name;
    }

    public GeoBone getParentBone() {
        return this.locator.parent;
    }

    public void setWorldSpacePosition(double x, double y, double z) {
        this.worldSpacePos = new Vec3d(x, y, z);
    }

    /**
     * This is the position of this locator in world space. It is completely
     * different per client, and thus is only meant for used for particles.
     * */
    public Vec3d getWorldSpacePosition() {
        return this.worldSpacePos;
    }

    /**
     * This is the position of this locator in model space. It is the same
     * per client, making it apt for things such as hitboxes and rider positions.
     * */
    public Vec3d getModelSpacePosition() {
        Vec3d boneDispOffset = this.getPositionOffsetFromBoneDisplacements();
        Vec3d boneRotOffset = this.getPositionOffsetFromBoneRotations();

        return new Vec3d(
                this.locator.getPositionX() + boneDispOffset.x + boneRotOffset.x,
                this.locator.getPositionY() + boneDispOffset.y + boneRotOffset.y,
                this.locator.getPositionZ() + boneDispOffset.z + boneRotOffset.z
        );
    }

    public void setWorldSpaceYXZQuaternion(Quaternion quaternion) {
        this.worldSpaceYXZQuaternion = quaternion;
    }
    /**
     * This is the quaternion for rotations of this locator in world space.
     * It is completely different per client, and thus is only meant for
     * used for particles.
     * */
    public Quaternion getWorldSpaceYXZQuaternion() {
        return this.worldSpaceYXZQuaternion;
    }

    /**
     * This is the quaternion for rotations of this locator in model space.
     * It is the same per client, making it apt for things such as hitboxes
     * and rider positions.
     * */
    public Quaternion getModelSpaceYXZQuaternion() {
        Quaternion toReturn = new Quaternion(0, 0, 0, 1);

        //setup for getting from ancestor -> from child
        ArrayList<GeoBone> chain = new ArrayList<>();
        for (GeoBone boneToTest = this.locator.parent; boneToTest != null; boneToTest = boneToTest.parent) chain.add(boneToTest);

        //multiply rotation quaternions in each chain
        for (int i = chain.size() - 1; i >= 0; i--) {
            GeoBone boneToTest = chain.get(i);
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
                this.locator.getRotationX(),
                this.locator.getRotationY(),
                -this.locator.getRotationZ()
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
        GeoBone boneToTest = this.locator.parent;

        while (boneToTest != null) {
            toReturn = toReturn.add(boneToTest.getPositionX(), boneToTest.getPositionY(), boneToTest.getPositionZ());
            boneToTest = boneToTest.parent;
        }
        return toReturn;
    }

    private Vec3d getPositionOffsetFromBoneRotations() {
        Vec3d vecPos = new Vec3d(this.locator.getPositionX(), this.locator.getPositionY(), this.locator.getPositionZ());
        GeoBone boneToTest = this.locator.parent;

        //evaluate
        while (boneToTest != null) {
            //get vector for direction from pivot to pos
            Vec3d vecPivot = new Vec3d(boneToTest.getPivotX(), boneToTest.getPivotY(), boneToTest.getPivotZ());
            Vec3d vecDirection = vecPos.subtract(vecPivot);

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
            Vec3d rotatedVecDirection = VectorUtils.convertQuaternionToVec(quatRotatedVecDirection);

            //update vecPos
            vecPos = rotatedVecDirection.add(vecPivot);
            boneToTest = boneToTest.parent;
        }

        return vecPos.subtract(this.locator.getPositionX(), this.locator.getPositionY(), this.locator.getPositionZ());
    }

    public void createParticleEmitter(ParticleBuilder builder) {
        RiftLibParticleEmitter emitterToAdd = new RiftLibParticleEmitter(builder, Minecraft.getMinecraft().world, this);
        ParticleTicker.EMITTER_LIST.add(emitterToAdd);
    }
}
