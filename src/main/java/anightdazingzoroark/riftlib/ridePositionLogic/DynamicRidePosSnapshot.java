package anightdazingzoroark.riftlib.ridePositionLogic;

import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DynamicRidePosSnapshot {
    @NotNull
    private final IDynamicRideUser<?> dynamicRideUser;
    @NotNull
    private final Map<Integer, Vec3d> passengerRenderPositions = new HashMap<>();
    @NotNull
    public final Set<Integer> renderingPassengers = new HashSet<>();
    @NotNull
    public Vec3d renderOriginVec = new Vec3d(0, 0, 0);
    private boolean storedDynamicRideEntity;
    private double posX;
    private double posY;
    private double posZ;
    private float rotationYaw;
    private float renderYawOffset;

    public DynamicRidePosSnapshot(@NotNull IDynamicRideUser<?> dynamicRideUser) {
        this.dynamicRideUser = dynamicRideUser;
    }

    public void storeSnapshot(float partialTicks, float finalYaw) {
        EntityLivingBase dynamicRideEntity = this.dynamicRideUser.getDynamicRideUser();
        if (!this.storedDynamicRideEntity) {
            this.posX = dynamicRideEntity.posX;
            this.posY = dynamicRideEntity.posY;
            this.posZ = dynamicRideEntity.posZ;
            this.rotationYaw = dynamicRideEntity.rotationYaw;
            this.renderYawOffset = dynamicRideEntity.renderYawOffset;
            this.storedDynamicRideEntity = true;
        }

        dynamicRideEntity.posX = Interpolations.lerp(dynamicRideEntity.lastTickPosX, dynamicRideEntity.posX, partialTicks);
        dynamicRideEntity.posY = Interpolations.lerp(dynamicRideEntity.lastTickPosY, dynamicRideEntity.posY, partialTicks);
        dynamicRideEntity.posZ = Interpolations.lerp(dynamicRideEntity.lastTickPosZ, dynamicRideEntity.posZ, partialTicks);
        dynamicRideEntity.rotationYaw = finalYaw;
        dynamicRideEntity.renderYawOffset = finalYaw;
    }

    public void restoreSnapshot() {
        if (!this.storedDynamicRideEntity) return;

        EntityLivingBase dynamicRideEntity = this.dynamicRideUser.getDynamicRideUser();
        dynamicRideEntity.posX = this.posX;
        dynamicRideEntity.posY = this.posY;
        dynamicRideEntity.posZ = this.posZ;
        dynamicRideEntity.rotationYaw = this.rotationYaw;
        dynamicRideEntity.renderYawOffset = this.renderYawOffset;
        this.storedDynamicRideEntity = false;
    }

    public void cachePassengerRidePositions() {
        EntityLivingBase dynamicRideEntity = this.dynamicRideUser.getDynamicRideUser();
        Entity controller = dynamicRideEntity.getControllingPassenger();
        this.dynamicRideUser.ridePosList().updatePositions();
        this.passengerRenderPositions.clear();

        if (this.dynamicRideUser.ridePosList().isEmpty()) return;

        List<Vec3d> otherPositions = this.dynamicRideUser.ridePosList().getPassengerWorldPositions();
        for (Entity passenger : dynamicRideEntity.getPassengers()) {
            if (controller != null && controller.equals(passenger)) {
                Vec3d controllerPos = this.dynamicRideUser.ridePosList().getControllerWorldPos();
                if (controllerPos != null) {
                    this.passengerRenderPositions.put(passenger.getEntityId(), new Vec3d(
                            controllerPos.x,
                            controllerPos.y + this.dynamicRideUser.passengerOffset(passenger),
                            controllerPos.z
                    ));
                }
            }
            else {
                int passengerPosIndex = this.dynamicRideUser.getPassengerPositionIndex(passenger);
                if (passengerPosIndex >= 0 && passengerPosIndex < otherPositions.size()) {
                    Vec3d ridePos = otherPositions.get(passengerPosIndex);
                    this.passengerRenderPositions.put(passenger.getEntityId(), new Vec3d(
                            ridePos.x,
                            ridePos.y + this.dynamicRideUser.passengerOffset(passenger),
                            ridePos.z
                    ));
                }
            }
        }
    }

    @Nullable
    public Vec3d getRidePosition(@NotNull Entity passenger) {
        return this.passengerRenderPositions.get(passenger.getEntityId());
    }

    public boolean isRenderingPassenger(@NotNull Entity passenger) {
        return this.renderingPassengers.contains(passenger.getEntityId());
    }
}
