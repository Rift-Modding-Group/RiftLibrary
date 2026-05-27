package anightdazingzoroark.riftlib.ridePositionLogic;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import org.lwjglx.util.vector.Quaternion;

import java.util.ArrayList;
import java.util.List;

public interface IDynamicRideUser<T extends EntityLivingBase & IAnimatable<AnimationDataEntity>> {
    /**
     * Get the parent. Must always return the entity its being implemented in.
     * */
    T getDynamicRideUser();

    default float dynamicRiderUserScale() {
        return 1f;
    }

    DynamicRidePosList ridePosList();

    default void updatePassenger(Entity passenger) {
        //block from non-passengers
        if (!this.getDynamicRideUser().isPassenger(passenger)) return;

        //do not update if there's no valid positions
        if (this.ridePosList().isEmpty()) return;

        //---start with the controller seat first---
        Entity controller = this.getDynamicRideUser().getControllingPassenger();
        Vec3d controllerPos = this.ridePosList().getControllerPos();

        if (controllerPos != null && controller != null && controller.equals(passenger)) {
            if (this.canRotateMounted()) {
                this.getDynamicRideUser().rotationYaw = passenger.rotationYaw;
                this.getDynamicRideUser().prevRotationYaw = this.getDynamicRideUser().rotationYaw;
                this.getDynamicRideUser().rotationPitch = passenger.rotationPitch * 0.5f;
                this.getDynamicRideUser().setRotation(this.getDynamicRideUser().rotationYaw, this.getDynamicRideUser().rotationPitch);
                this.getDynamicRideUser().renderYawOffset = this.getDynamicRideUser().rotationYaw;
            }

            passenger.setPosition(controllerPos.x, controllerPos.y + this.passengerOffset(passenger), controllerPos.z);

            ((EntityLivingBase) passenger).renderYawOffset = this.getDynamicRideUser().renderYawOffset;
        }

        //---now deal with other positions---
        List<Vec3d> otherPositions = this.ridePosList().getPassengerPositions();

        if (!otherPositions.isEmpty() && !passenger.equals(controller)) {
            for (Vec3d otherPos : otherPositions) {
                passenger.setPosition(otherPos.x, otherPos.y + this.passengerOffset(passenger), otherPos.z);
            }
        }

        //---dismount passenger if ded---
        if (!this.getDynamicRideUser().isEntityAlive()) passenger.dismountRidingEntity();
    }

    default float passengerOffset(Entity entity) {
        if (entity instanceof EntityPlayer) return -0.6f;
        return 0f;
    }

    default boolean canRotateMounted() {
        return true;
    }
}
