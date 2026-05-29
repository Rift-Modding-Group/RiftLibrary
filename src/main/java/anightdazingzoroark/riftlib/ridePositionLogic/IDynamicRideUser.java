package anightdazingzoroark.riftlib.ridePositionLogic;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public interface IDynamicRideUser<T extends EntityLivingBase & IAnimatable<AnimationDataEntity>> {
    /**
     * Get the parent. Must always return the entity its being implemented in.
     * */
    T getDynamicRideUser();

    /**
     * Get the model scale of the user.
     * */
    default float dynamicRiderUserScale() {
        return 1f;
    }

    /**
     * The ride position list is very important, it uses the AnimatedLocators of an IAnimatable
     * to define where a rider will go to upon riding.
     * */
    DynamicRidePosList ridePosList();

    /**
     * Put this in the Entity.updatePassenger() method in the entity you're implementing this in.
     * */
    default void updatePassenger(Entity passenger) {
        //---make sure to only update for true passengers---
        if (!this.getDynamicRideUser().isPassenger(passenger)) return;

        //---dismount passenger if ded---
        if (!this.getDynamicRideUser().isEntityAlive()) {
            passenger.dismountRidingEntity();
            return;
        }

        //---update positions, plus do not update if there's no valid positions---
        this.ridePosList().updatePositions();
        if (this.ridePosList().isEmpty()) return;

        //---start with the controller seat first---
        Entity controller = this.getDynamicRideUser().getControllingPassenger();
        Vec3d controllerPos = this.ridePosList().getControllerWorldPos();
        if (controllerPos != null && controller != null && controller.equals(passenger)) {
            if (this.canRotateMounted()) {
                this.getDynamicRideUser().rotationYaw = passenger.rotationYaw;
                this.getDynamicRideUser().prevRotationYaw = this.getDynamicRideUser().rotationYaw;
                this.getDynamicRideUser().rotationPitch = passenger.rotationPitch * 0.5f;
                this.getDynamicRideUser().setRotation(this.getDynamicRideUser().rotationYaw, this.getDynamicRideUser().rotationPitch);
                this.getDynamicRideUser().renderYawOffset = this.getDynamicRideUser().rotationYaw;
            }

            this.applyRidePosition(passenger, controllerPos);

            if (passenger instanceof EntityLivingBase) {
                ((EntityLivingBase) passenger).renderYawOffset = this.getDynamicRideUser().renderYawOffset;
            }
        }
        //---now deal with other positions for other passengers---
        else {
            List<Vec3d> otherPositions = this.ridePosList().getPassengerWorldPositions();
            int passengerPosIndex = this.getPassengerPositionIndex(passenger);

            if (passengerPosIndex >= 0 && passengerPosIndex < otherPositions.size()) {
                this.applyRidePosition(passenger, otherPositions.get(passengerPosIndex));
            }
        }
    }

    default int getPassengerPositionIndex(Entity passenger) {
        Entity controller = this.getDynamicRideUser().getControllingPassenger();
        int passengerPosIndex = 0;

        for (Entity currentPassenger : this.getDynamicRideUser().getPassengers()) {
            if (currentPassenger.equals(controller)) continue;
            if (currentPassenger.equals(passenger)) return passengerPosIndex;
            passengerPosIndex++;
        }

        return -1;
    }

    default void applyRidePosition(Entity passenger, Vec3d ridePos) {
        passenger.setPosition(ridePos.x, ridePos.y + this.passengerOffset(passenger), ridePos.z);
    }

    default float passengerOffset(Entity entity) {
        if (entity instanceof EntityPlayer) return -0.6f;
        return 0f;
    }

    default boolean canRotateMounted() {
        return true;
    }
}
