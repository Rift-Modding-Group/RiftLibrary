package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.*;

/**
 * Meant to be simultaneously created on the server and the client. This defines "rays",
 * which are essentially vectors that affect any blocks or entities that cross
 * them.
 * */
public class RiftLibRay {
    @NotNull
    private final IRayCreator<?> rayCreator;
    @NotNull
    public final String rayName;
    @NotNull
    public final AnimatedLocator parentLocator;
    @NotNull
    public final RiftLibRayBuilder builder;

    //the origin of the ray. is nullable so that upon initialization there
    //will not be a giant puddle below the user
    @Nullable
    private Vec3d originPos;
    //the direction the ray must travel in
    @NotNull
    private Vec3d directionVector = new Vec3d(0, 0, -1);

    //all the segments in a ray
    private final List<RiftLibRaySegment> raySegmentList = new ArrayList<>();

    //flag to signal the start of the end of the ray
    private boolean isEnded;
    //flag to signal removal of the ray
    private boolean isDead;

    //how many fields can this shit even hold anymore
    //might as well pass the builder by this point
    public RiftLibRay(@NotNull IRayCreator<?> rayCreator, @NotNull String rayName, @NotNull AnimatedLocator parentLocator, @NotNull RiftLibRayBuilder builder) {
        this.rayCreator = rayCreator;
        this.rayName = rayName;
        this.parentLocator = parentLocator;
        this.builder = builder;
    }

    /**
     * This updates the ray and is meant to be called on both sides.
     * */
    public void onUpdate() {
        //-----kill the ray and its segments if the ray creator died for some reason-----
        if (!this.rayCreator.getRayCreator().isEntityAlive()) {
            for (RiftLibRaySegment raySegment : this.raySegmentList) raySegment.killSegment();
            this.killRay();
        }

        //-----do not update if ray is dead-----
        if (this.isDead) return;

        //-----define the ray origin pos and direction vector when not fading out-----
        if (!this.isEnded) {
            //---common stuff---
            //determine entity yaw and turn into quaternion
            double normalYawRadians = -Math.toRadians(this.rayCreator.getRayCreator().rotationYawHead);
            double riddenYawRadians = -Math.toRadians(this.rayCreator.getRayCreator().rotationYaw);
            double finalYawRadians = this.rayCreator.getRayCreator().isBeingRidden() ? riddenYawRadians : normalYawRadians;

            //---for origin---
            //set initial entity offset from center, in model space ofc
            Vec3d animatedLocatorPos = this.parentLocator.getModelSpacePosition();
            Vec3d posVec = new Vec3d(
                    -animatedLocatorPos.x / 16f * this.rayCreator.rayCreatorScale(),
                    animatedLocatorPos.y / 16f * this.rayCreator.rayCreatorScale(),
                    -animatedLocatorPos.z / 16f * this.rayCreator.rayCreatorScale()
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
            //correct quaternion for animated locator to work in world space
            Quaternion correction = QuaternionUtils.createYXZQuaternion(Math.PI / 2D, Math.PI, 0);
            Quaternion correctedLocatorQuat = new Quaternion();
            Quaternion.mul(this.parentLocator.getModelSpaceYXZQuaternion(), correction, correctedLocatorQuat);
            Quaternion.normalise(correctedLocatorQuat, correctedLocatorQuat);

            //proceed
            Vec3d forwardVec = new Vec3d(0, 0, -1); //good ol point northward vector
            forwardVec = VectorUtils.rotateVectorWithQuaternion(forwardVec, correctedLocatorQuat).normalize();
            forwardVec = VectorUtils.rotateVectorWithQuaternion(forwardVec, QuaternionUtils.createYXZQuaternion(0, finalYawRadians, 0)).normalize();
            this.directionVector = forwardVec;
        }

        //-----update segment list-----
        Iterator<RiftLibRaySegment> it = this.raySegmentList.iterator();
        while (it.hasNext()) {
            RiftLibRaySegment raySegment = it.next();

            //update
            raySegment.onUpdate();
            if (this.builder.followUserRotation()) raySegment.updateDirectionVector(this.directionVector);
        }

        //-----create a new segment, rate is dependent on ray speed-----
        if (!this.isEnded && this.originPos != null) {
            this.raySegmentList.add(new RiftLibRaySegment(this.rayCreator, this.originPos, this.directionVector, this.builder));

            if (this.builder.getOnlyOneSegment()) this.endRay();
        }

        //-----send ray information to the ray creator-----
        if (!this.rayCreator.getRayCreator().world.isRemote && this.originPos != null) {
            this.rayCreator.applyRaySegments(
                    this.rayName, new BlockPos(this.originPos),
                    new RayHitResult(this.getEntitiesInRaySegments(), this.getBlockPositionsInRaySegments())
            );
        }

        //-----remove from list of segments if dead after callers had a tick to consume their final state-----
        it = this.raySegmentList.iterator();
        while (it.hasNext()) {
            RiftLibRaySegment raySegment = it.next();
            if (raySegment.isDead()) it.remove();
        }

        if (this.isEnded && this.raySegmentList.isEmpty()) this.killRay();
    }

    /**
     * Use this to end this ray to make it fade out.
     * */
    public void endRay() {
        this.isEnded = true;
    }

    /**
     * Use this to kill this ray and make it just disappear.
     * */
    public void killRay() {
        this.isDead = true;
    }

    public boolean isDead() {
        return this.isDead;
    }

    public List<AxisAlignedBB> getSegmentAABBList() {
        List<AxisAlignedBB> toReturn = new ArrayList<>();
        for (RiftLibRaySegment raySegment : this.raySegmentList) {
            toReturn.addAll(raySegment.getSegmentAABBList());
        }
        return toReturn;
    }

    public List<RiftLibRaySegment.DebugLine> getDebugGridLines() {
        List<RiftLibRaySegment.DebugLine> toReturn = new ArrayList<>();
        for (RiftLibRaySegment raySegment : this.raySegmentList) {
            if (raySegment.isDead()) continue;
            toReturn.addAll(raySegment.getDebugGridLines());
        }

        return toReturn;
    }

    private Set<Entity> getEntitiesInRaySegments() {
        Set<Entity> toReturn = new HashSet<>();
        World world = this.rayCreator.getRayCreator().world;

        for (RiftLibRaySegment raySegment : this.raySegmentList) {
            for (AxisAlignedBB aabb : raySegment.getSegmentAABBList()) {
                List<Entity> entityList = world.getEntitiesWithinAABB(Entity.class, aabb);
                for (Entity entity : entityList) {
                    if (entity.equals(this.rayCreator.getRayCreator())) continue;
                    if (toReturn.contains(entity)) continue;
                    if (!raySegment.intersectsEntity(entity)) continue;
                    toReturn.add(entity);
                }
            }
        }

        return toReturn;
    }

    private Set<BlockPos> getBlockPositionsInRaySegments() {
        Set<BlockPos> toReturn = new HashSet<>();

        for (RiftLibRaySegment raySegment : this.raySegmentList) {
            toReturn.addAll(raySegment.getBlockPositionsInCurrentShape());
        }

        return toReturn;
    }

    /**
     * This ray result is to be immutable and to be sent to the server.
     * */
    public record RayHitResult(Set<Entity> hitEntities, Set<BlockPos> hitBlockPositions) {}
}
