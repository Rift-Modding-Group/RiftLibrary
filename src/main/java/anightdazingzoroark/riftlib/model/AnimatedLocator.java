package anightdazingzoroark.riftlib.model;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.geo.GeoBone;
import anightdazingzoroark.riftlib.geo.GeoLocator;
import anightdazingzoroark.riftlib.particle.ParticleInformation;
import anightdazingzoroark.riftlib.particle.ParticleTicker;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a wrapper for GeoLocator that allows for things
 * such as quaternions or world space positions.
 * */
public class AnimatedLocator {
    private final GeoLocator locator;
    private final AbstractAnimationData<?, ?> animationData;
    //---world space stuff---
    @NotNull
    private Vec3d worldSpacePos = new Vec3d(0, 0, 0);
    @NotNull
    private Quaternionf worldSpaceYXZQuaternion = new Quaternionf();
    private boolean isUpdated = true;

    public AnimatedLocator(GeoLocator geoLocator, AbstractAnimationData<?, ?> animationData) {
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

    @NotNull
    public AbstractAnimationData<?, ?> getAnimationData() {
        return this.animationData;
    }

    public String getName() {
        return this.locator.name;
    }

    //-----model space stuff starts here-----
    @NotNull
    public Vec3d getModelSpacePosition() {
        Vec3d boneDispOffset = this.getPositionOffsetFromBoneDisplacements();
        Vec3d boneRotOffset = this.getPositionOffsetFromBoneRotations();
        Vec3d locatorPos = this.getLocatorPosition();

        return new Vec3d(
                locatorPos.x + boneDispOffset.x + boneRotOffset.x,
                locatorPos.y + boneDispOffset.y + boneRotOffset.y,
                locatorPos.z + boneDispOffset.z + boneRotOffset.z
        );
    }

    @NotNull
    public Quaternionf getModelSpaceYXZQuaternion() {
        Quaternionf toReturn = new Quaternionf(0, 0, 0, 1);

        //setup for getting from ancestor -> from child
        ArrayList<GeoBone> chain = new ArrayList<>();
        for (GeoBone boneToTest = this.locator.parent; boneToTest != null; boneToTest = boneToTest.parent) chain.add(boneToTest);

        //multiply rotation quaternions in each chain
        for (int i = chain.size() - 1; i >= 0; i--) {
            GeoBone boneToTest = chain.get(i);
            Quaternionf quatBone = QuaternionUtils.createYXZQuaternion(
                    boneToTest.getRotation().x,
                    boneToTest.getRotation().y,
                    boneToTest.getRotation().z
            );

            quatBone.normalize();

            toReturn.mul(quatBone);
        }

        //now apply the locator's rotation
        Quaternionf quatLocator = QuaternionUtils.createYXZQuaternion(
                this.locator.getRotation().x,
                this.locator.getRotation().y,
                this.locator.getRotation().z
        );

        quatLocator.normalize();

        toReturn.mul(quatLocator);

        //normalize and return
        toReturn.normalize();

        return toReturn;
    }
    //-----model space stuff ends here-----

    //-----world space stuff starts here-----
    @SideOnly(Side.CLIENT)
    public void setWorldSpacePosition(double x, double y, double z) {
        this.worldSpacePos = new Vec3d(x, y, z);
    }

    /**
     * This is the position of this locator in world space. It is completely
     * different per client, and thus is only meant for used for particles.
     * */
    @SideOnly(Side.CLIENT)
    public Vec3d getWorldSpacePosition() {
        return this.worldSpacePos;
    }

    @SideOnly(Side.CLIENT)
    public void setWorldSpaceYXZQuaternion(@NotNull Quaternionf quaternion) {
        this.worldSpaceYXZQuaternion = quaternion;
    }

    /**
     * This is the quaternion for rotations of this locator in world space.
     * It is completely different per client, and thus is only meant for
     * used for particles.
     * */
    @SideOnly(Side.CLIENT)
    @NotNull
    public Quaternionf getWorldSpaceYXZQuaternion() {
        return this.worldSpaceYXZQuaternion;
    }
    //-----world space stuff ends here-----

    private Vec3d getPositionOffsetFromBoneDisplacements() {
        Vec3d toReturn = Vec3d.ZERO;
        GeoBone boneToTest = this.locator.parent;

        while (boneToTest != null) {
            toReturn = toReturn.add(boneToTest.getPosition().x, boneToTest.getPosition().y, boneToTest.getPosition().z);
            boneToTest = boneToTest.parent;
        }
        return toReturn;
    }

    private Vec3d getPositionOffsetFromBoneRotations() {
        Vec3d locatorPos = this.getLocatorPosition();
        Vec3d vecPos = locatorPos;

        List<GeoBone> chain = new ArrayList<>();
        for (GeoBone boneToTest = this.locator.parent; boneToTest != null; boneToTest = boneToTest.parent) chain.add(boneToTest);

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
            Quaternionf quatBoneRot = QuaternionUtils.createXYZQuaternion(
                    boneToTest.getRotation().x,
                    boneToTest.getRotation().y,
                    boneToTest.getRotation().z
            );
            quatBoneRot.normalize();

            Quaternionf quatBoneRotConj = new Quaternionf();
            quatBoneRot.conjugate(quatBoneRotConj);

            //rotate vecDirection
            Quaternionf quatVecDirection = VectorUtils.convertToQuaternion(vecDirection);
            Quaternionf temp = new Quaternionf();
            Quaternionf quatRotatedVecDirection = new Quaternionf();
            quatBoneRot.mul(quatVecDirection, temp);
            temp.mul(quatBoneRotConj, quatRotatedVecDirection);
            Vec3d rotatedVecDirection = VectorUtils.convertQuaternionToVec(quatRotatedVecDirection);

            //update vecPos
            vecPos = rotatedVecDirection.add(vecPivot);
        }

        return vecPos.subtract(locatorPos);
    }

    private Vec3d getLocatorPosition() {
        return new Vec3d(
                this.locator.getPosition().x,
                this.locator.getPosition().y,
                this.locator.getPosition().z
        );
    }

    public RiftLibParticleEmitter createParticleEmitter(ParticleInformation builder) {
        RiftLibParticleEmitter emitterToAdd = new RiftLibParticleEmitter(builder, Minecraft.getMinecraft().world, this);
        ParticleTicker.EMITTER_LIST.add(emitterToAdd);
        return emitterToAdd;
    }
}
