package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.util.MathUtils;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.lwjglx.util.vector.Quaternion;

import java.util.ArrayList;
import java.util.List;

/**
 * Meant to be created on the client. This is more or less a helper class that allows for
 * "rays", which are essentially vectors that affect any blocks or entities that cross
 * them.
 * <br /><br />
 * TODO: edit this so that when a ray hits a block, the ray spreads out on the surface of the block, and this spread is dependent on length and dist from origin.
 * */
public class RiftLibRay {
    @NotNull
    private final IRayCreator<?> rayCreator;
    @NotNull
    public final String rayName;
    @NotNull
    private final String parentLocatorName;
    private final double maxRayLength;
    private final double rayWidth;
    private final double rayCreationTime;
    private final double rayFadeOutTime;

    //this represents the phase the ray is currently in
    @NotNull
    private CreationPhase creationPhase;
    //how much time has passed since the change in creation phase in ticks
    private int tick;

    //the origin of the ray
    @NotNull
    private Vec3d originPos = new Vec3d(0, 0, 0);
    @NotNull
    private Vec3d fadeEndPos = new Vec3d(0, 0, 0);
    @NotNull
    private Vec3d directionVector = new Vec3d(0, 0, 1);
    private double currentLength;
    private double fadeStartLength;

    private boolean isDead;

    public RiftLibRay(@NotNull IRayCreator<?> rayCreator, @NotNull String rayName, @NotNull String parentLocatorName, double maxRayLength, double rayWidth, double rayCreationTime, double rayFadeOutTime) {
        this.rayCreator = rayCreator;
        this.rayName = rayName;
        this.parentLocatorName = parentLocatorName;
        this.maxRayLength = maxRayLength;
        this.rayWidth = rayWidth;
        this.rayCreationTime = rayCreationTime * 20D;
        this.rayFadeOutTime = rayFadeOutTime * 20D;

        this.creationPhase = CreationPhase.CREATE;
    }

    public void onUpdate() {
        //-----kill the ray if the ray creator suddenly died for some reason-----
        if (!this.rayCreator.getRayCreator().isEntityAlive()) this.killRay();

        if (this.isDead) return;

        AnimatedLocator animatedLocator = this.rayCreator.getRayCreator().getAnimationData().getAnimatedLocator(this.parentLocatorName);

        //-----define the ray origin pos and direction vector when not fading out-----
        if (this.creationPhase != CreationPhase.FADE_OUT) {
            //---common stuff---
            //determine entity yaw and turn into quaternion
            double normalYawRadians = -Math.toRadians(this.rayCreator.getRayCreator().rotationYawHead);
            double riddenYawRadians = -Math.toRadians(this.rayCreator.getRayCreator().rotationYaw);
            double finalYawRadians = this.rayCreator.getRayCreator().isBeingRidden() ? riddenYawRadians : normalYawRadians;

            //define locator quaternion
            Quaternion animatedLocatorQuat = animatedLocator.getModelSpaceYXZQuaternion();

            //---for origin---
            //set initial entity offset from center, in model space ofc
            Vec3d modelSpacePos = animatedLocator.getModelSpacePosition();
            Vec3d posVec = new Vec3d(
                    modelSpacePos.x / 16f * this.rayCreator.rayCreatorScale(),
                    modelSpacePos.y / 16f * this.rayCreator.rayCreatorScale(),
                    -modelSpacePos.z / 16f * this.rayCreator.rayCreatorScale()
            );

            //rotate the locator position only by the entity yaw; the locator's own rotation affects direction, not origin
            posVec = VectorUtils.rotateVectorWithQuaternion(posVec, QuaternionUtils.createXYZQuaternion(0, finalYawRadians, 0));

            //position to entity pos
            posVec = new Vec3d(
                    posVec.x + this.rayCreator.getRayCreator().posX,
                    posVec.y + this.rayCreator.getRayCreator().posY,
                    posVec.z + this.rayCreator.getRayCreator().posZ
            );

            //set final pos vec
            this.originPos = posVec;

            //---for direction vector---
            Vec3d forwardVec = new Vec3d(0, 0, 1);
            forwardVec = VectorUtils.rotateVectorWithQuaternion(forwardVec, animatedLocatorQuat).normalize();
            forwardVec = VectorUtils.rotateVectorWithQuaternion(forwardVec, QuaternionUtils.createYXZQuaternion(0, finalYawRadians, 0)).normalize();
            forwardVec = new Vec3d(forwardVec.x, -forwardVec.y, forwardVec.z);
            this.directionVector = forwardVec;
        }

        //-----define the current length of the ray, lengthens on creation and shortens on fade out-----
        if (this.creationPhase == CreationPhase.CREATE) {
            this.currentLength = MathUtils.slopeResult(this.tick, true, 0, this.rayCreationTime, 0, this.maxRayLength);

            if (this.tick >= this.rayCreationTime) this.changeCreationPhase(CreationPhase.RUN);
        }
        else if (this.creationPhase == CreationPhase.FADE_OUT) {
            this.currentLength = MathUtils.slopeResult(this.tick, true, 0, this.rayFadeOutTime, this.fadeStartLength, 0);

            if (this.tick >= this.rayFadeOutTime) this.isDead = true;
        }

        //-----define the ray origin pos when fading out-----
        if (this.creationPhase == CreationPhase.FADE_OUT) {
            this.originPos = this.fadeEndPos.subtract(this.directionVector.scale(this.currentLength));
        }

        //-----ticking is required on creating or fading out the ray but useless outside of that-----
        if (this.creationPhase != CreationPhase.RUN && !this.isDead) this.tick++;
    }

    /**
     * Use this to end this ray to make it fade out.
     * */
    public void endRay() {
        this.fadeStartLength = this.currentLength;
        this.fadeEndPos = this.originPos.add(this.directionVector.scale(this.fadeStartLength));
        this.changeCreationPhase(CreationPhase.FADE_OUT);
    }

    /**
     * Use this to kill this ray and make it just disappear.
     * */
    public void killRay() {
        this.isDead = true;
    }

    //better way to change the creation phase
    private void changeCreationPhase(@NotNull CreationPhase phase) {
        this.creationPhase = phase;
        this.tick = 0;
    }

    public boolean isDead() {
        return this.isDead;
    }

    /**
     * Is for converting RiftLibRay information into a list of AxisAlignedBB instances
     * that will be sent to the server.
     * */
    public List<AxisAlignedBB> createAABBListFromRay() {
        List<AxisAlignedBB> toReturn = new ArrayList<>();

        Vec3d currentCenterPos = this.originPos;
        int maxLength = (int) Math.ceil(this.currentLength);
        double width = this.rayWidth;
        int stepDist = 1; //how many steps between the center of each aabb to create
        Vec3d stepVec = this.directionVector.scale(stepDist);

        for (int step = 0; step < maxLength; step += stepDist) {
            //create aabb and add
            AxisAlignedBB toAdd = new AxisAlignedBB(
                    currentCenterPos.x - width / 2D,
                    currentCenterPos.y - width / 2D,
                    currentCenterPos.z - width / 2D,
                    currentCenterPos.x + width / 2D,
                    currentCenterPos.y + width / 2D,
                    currentCenterPos.z + width / 2D
            );
            toReturn.add(toAdd);

            //modify currentCenterPos to go to next position
            currentCenterPos = currentCenterPos.add(stepVec);
        }

        return toReturn;
    }

    private enum CreationPhase {
        CREATE,
        RUN,
        FADE_OUT;
    }

    /**
     * A ray builder is like a template but for the rays to create.
     * */
    public record Builder (
            @NotNull IRayCreator<?> rayCreator, @NotNull String parentLocatorName,
            double maxRayLength, double rayWidth, double rayCreationTime, double rayFadeOutTime
    ) {}
}
