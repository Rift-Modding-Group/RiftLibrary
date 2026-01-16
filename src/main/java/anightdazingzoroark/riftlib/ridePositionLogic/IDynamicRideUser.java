package anightdazingzoroark.riftlib.ridePositionLogic;

import anightdazingzoroark.riftlib.RiftLibLinkerRegistry;
import anightdazingzoroark.riftlib.core.IAnimatable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public interface IDynamicRideUser {
    //get the parent
    //must always return the entity its being implemented in
    //so its return statement in the entity implementing this should be "return this;"
    EntityLiving getDynamicRideUser();

    DynamicRidePosList ridePosList();

    void setRidePosition(DynamicRidePosList ridePosList);

    default Vec3d rotateOffset(Vec3d offset) {
        double xOffset = offset.x * ((IAnimatable) this.getDynamicRideUser()).scale();
        double yOffset = offset.y * ((IAnimatable) this.getDynamicRideUser()).scale();
        double zOffset = offset.z * ((IAnimatable) this.getDynamicRideUser()).scale();

        double radians = Math.toRadians(this.getDynamicRideUser().rotationYaw);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double rotatedX = xOffset * cos - zOffset * sin;
        double rotatedZ = xOffset * sin + zOffset * cos;

        return new Vec3d(rotatedX, yOffset, rotatedZ);
    }

    default void updatePassenger(Entity passenger) {
        if (this.ridePosList() == null || this.ridePosList().isEmpty()) return;

        //start with the controller seat first
        Vec3d controllerPos = this.ridePosList().getControllerPos();

        if (controllerPos != null
                && this.getDynamicRideUser().getControllingPassenger() != null
                && this.getDynamicRideUser().getControllingPassenger().equals(passenger)) {
            if (this.canRotateMounted()) {
                this.getDynamicRideUser().rotationYaw = passenger.rotationYaw;
                this.getDynamicRideUser().prevRotationYaw = this.getDynamicRideUser().rotationYaw;
                this.getDynamicRideUser().rotationPitch = passenger.rotationPitch * 0.5f;
                this.getDynamicRideUser().setRotation(this.getDynamicRideUser().rotationYaw, this.getDynamicRideUser().rotationPitch);
                this.getDynamicRideUser().renderYawOffset = this.getDynamicRideUser().rotationYaw;
            }

            passenger.setPosition(
                    this.getDynamicRideUser().posX + this.rotateOffset(controllerPos).x,
                    this.getDynamicRideUser().posY + this.rotateOffset(controllerPos).y + this.playerRideOffset(passenger),
                    this.getDynamicRideUser().posZ + this.rotateOffset(controllerPos).z
            );

            ((EntityLivingBase)passenger).renderYawOffset = this.getDynamicRideUser().renderYawOffset;
        }

        //now deal with other positions
        List<Vec3d> otherPositions = this.ridePosList().getOrderedPassengerPosList();

        if (!otherPositions.isEmpty() && !passenger.equals(this.getDynamicRideUser().getControllingPassenger())) {
            for (Vec3d otherPos : otherPositions)
                passenger.setPosition(
                        this.getDynamicRideUser().posX + this.rotateOffset(otherPos).x,
                        this.getDynamicRideUser().posY + this.rotateOffset(otherPos).y + this.playerRideOffset(passenger),
                        this.getDynamicRideUser().posZ + this.rotateOffset(otherPos).z
                );
        }

        if (this.getDynamicRideUser().isDead) passenger.dismountRidingEntity();
    }

    default float playerRideOffset(Entity entity) {
        if (entity instanceof EntityPlayer) return -0.6f;
        return 0f;
    }

    default boolean canRotateMounted() {
        return true;
    }
}
