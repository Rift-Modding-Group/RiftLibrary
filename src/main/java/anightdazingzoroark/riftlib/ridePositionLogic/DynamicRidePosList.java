package anightdazingzoroark.riftlib.ridePositionLogic;

import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.ArrayList;
import java.util.List;

public class DynamicRidePosList {
    @NotNull
    private final IDynamicRideUser<?> dynamicRideUser;
    @NotNull
    private final AnimationDataEntity animData;

    //positions for controller
    @Nullable
    private Vec3d controllerWorldPos;

    //positions for other passengers
    @NotNull
    private List<Vec3d> passengerWorldPositions = new ArrayList<>();
    @NotNull
    public final DynamicRidePosSnapshot snapshot;

    public DynamicRidePosList(@NotNull IDynamicRideUser<?> dynamicRideUser, @NotNull AnimationDataEntity animData) {
        this.dynamicRideUser = dynamicRideUser;
        this.animData = animData;
        this.snapshot = new DynamicRidePosSnapshot(dynamicRideUser);
    }

    public void updatePositions() {
        EntityLivingBase dynamicRideEntity = this.dynamicRideUser.getDynamicRideUser();
        if (!dynamicRideEntity.world.isRemote) {
            ServerModelRegistry.requireServerModel((IAnimatable<?>) dynamicRideEntity, "server ride positions");
        }

        this.controllerWorldPos = this.getRidePosFromLocator(this.dynamicRideUser.locatorControllerPosition());

        List<Vec3d> newPassengerWorldPositions = new ArrayList<>();
        for (String posName : this.dynamicRideUser.locatorRidePositions()) {
            Vec3d passengerRidePos = this.getRidePosFromLocator(posName);
            if (passengerRidePos == null) continue;
            newPassengerWorldPositions.add(passengerRidePos);
        }
        this.passengerWorldPositions = newPassengerWorldPositions;
    }

    /**
     * This transforms a declared locator's model-space position into its current world position.
     */
    @Nullable
    private Vec3d getRidePosFromLocator(@Nullable String locatorName) {
        if (locatorName == null) return null;

        AnimatedLocator animLocator = this.animData.getAnimatedLocator(locatorName);
        if (animLocator == null) return null;

        EntityLivingBase dynamicRideUser = this.dynamicRideUser.getDynamicRideUser();

        //correct locator position first
        Vec3d modelSpacePos = animLocator.getModelSpacePosition();
        Vec3d posVec = new Vec3d(
                -(float) modelSpacePos.x / 16f,
                (float) modelSpacePos.y / 16f,
                -(float) modelSpacePos.z / 16f
        );

        //change based on scale
        float parentScale = this.animData.getScale();
        posVec = new Vec3d(posVec.x * parentScale, posVec.y * parentScale, posVec.z * parentScale);

        //change based on yaw of ridden mob
        double yawRadians = -Math.toRadians(dynamicRideUser.rotationYaw);
        Quaternion quaternion = QuaternionUtils.createXYZQuaternion(0, yawRadians, 0);
        posVec = VectorUtils.rotateVectorWithQuaternion(posVec, quaternion);

        //reposition to ridden mob
        posVec = new Vec3d(
                posVec.x + dynamicRideUser.posX,
                posVec.y + dynamicRideUser.posY,
                posVec.z + dynamicRideUser.posZ
        );

        //return
        return posVec;
    }

    //todo: remove this and make passenger position application be here instead
    @NotNull
    public List<Vec3d> getPassengerWorldPositions() {
        return this.passengerWorldPositions;
    }

    @Nullable
    public Vec3d getControllerWorldPos() {
        return this.controllerWorldPos;
    }

    /**
     * If true, there's no rideable positions to use.
     * */
    public boolean isEmpty() {
        return this.controllerWorldPos == null && this.passengerWorldPositions.isEmpty();
    }
}
