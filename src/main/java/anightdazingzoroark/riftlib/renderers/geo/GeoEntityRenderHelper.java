package anightdazingzoroark.riftlib.renderers.geo;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationDataAdvancedEntity;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationDataEntity;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.ridePositionLogic.DynamicRidePosSnapshot;
import anightdazingzoroark.riftlib.ridePositionLogic.IDynamicRideUser;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.lwjglx.util.vector.Quaternion;

/**
 * Shared transforms for living and non-living entity renderers.
 * */
@SuppressWarnings("unchecked")
public class GeoEntityRenderHelper {
    public static <A extends Entity & IAnimatable<? extends AbstractAnimationDataAdvancedEntity<?, ?>>> void repositionAnimatedLocators(
            @NotNull A entity, float partialTicks
    ) {
        AbstractAnimationDataAdvancedEntity<?, ?> data = entity.getAnimationData();
        double entityX = Interpolations.lerp(entity.prevPosX, entity.posX, partialTicks);
        double entityY = Interpolations.lerp(entity.prevPosY, entity.posY, partialTicks);
        double entityZ = Interpolations.lerp(entity.prevPosZ, entity.posZ, partialTicks);
        float renderYaw = data.getModelYaw(partialTicks);
        if (entity.isBeingRidden() && entity instanceof IDynamicRideUser<?> rideUser) {
            renderYaw = rideUser.getRenderYaw(partialTicks);
        }

        double yawRadians = -Math.toRadians(renderYaw);
        Quaternion entityYaw = QuaternionUtils.createYXZQuaternion(0D, yawRadians, 0D);
        Quaternion modelToWorldRotation = QuaternionUtils.createYXZQuaternion(0D, Math.PI + yawRadians, 0D);
        float scale = data.getScale();
        for (AnimatedLocator locator : data.getAnimatedLocators().values()) {
            Vec3d modelPosition = locator.getModelSpacePosition();
            Vec3d relativePosition = VectorUtils.rotateVectorWithQuaternion(new Vec3d(
                    -modelPosition.x / 16D * scale,
                    modelPosition.y / 16D * scale,
                    -modelPosition.z / 16D * scale
            ), entityYaw);
            locator.setWorldSpacePosition(entityX + relativePosition.x, entityY + relativePosition.y, entityZ + relativePosition.z);

            Quaternion worldRotation = new Quaternion();
            Quaternion.mul(modelToWorldRotation, locator.getModelSpaceYXZQuaternion(), worldRotation);
            Quaternion.normalise(worldRotation, worldRotation);
            locator.setWorldSpaceYXZQuaternion(worldRotation);
        }
    }

    public static void renderPassengers(
            @NotNull RenderManager manager, @NotNull Entity entity, @NotNull DynamicRidePosSnapshot snapshot,
            float renderYaw, float partialTicks
    ) {
        float renderYawOffset = 0;
        float prevRenderYawOffset = 0;
        if (entity instanceof EntityLivingBase livingMount) {
            renderYawOffset = livingMount.renderYawOffset;
            prevRenderYawOffset = livingMount.prevRenderYawOffset;
            livingMount.renderYawOffset = renderYaw;
            livingMount.prevRenderYawOffset = renderYaw;
        }

        for (Entity passenger : entity.getPassengers()) {
            Vec3d position = snapshot.getRidePosition(passenger);
            if (position == null) continue;
            if (passenger == Minecraft.getMinecraft().player && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) continue;

            //save the passenger's pose before applying the mount's render yaw.
            boolean isController = passenger.equals(entity.getControllingPassenger());
            float passengerYaw = Interpolations.lerpYaw(passenger.prevRotationYaw, passenger.rotationYaw, partialTicks);
            float headYaw = 0;
            float prevHeadYaw = 0;
            if (isController) {
                passengerYaw = renderYaw;
                if (passenger instanceof EntityLivingBase livingPassenger) {
                    headYaw = livingPassenger.rotationYawHead;
                    prevHeadYaw = livingPassenger.prevRotationYawHead;
                    livingPassenger.rotationYawHead = renderYaw;
                    livingPassenger.prevRotationYawHead = renderYaw;
                }
            }

            //allow this passenger's render at its cached ride position.
            snapshot.renderingPassengers.add(passenger.getEntityId());
            manager.renderEntity(passenger,
                    position.x - snapshot.renderOriginVec.x,
                    position.y - snapshot.renderOriginVec.y,
                    position.z - snapshot.renderOriginVec.z,
                    passengerYaw, partialTicks, false);
            snapshot.renderingPassengers.remove(passenger.getEntityId());

            //restore the passenger's pose after rendering.
            if (isController && passenger instanceof EntityLivingBase livingPassenger) {
                livingPassenger.rotationYawHead = headYaw;
                livingPassenger.prevRotationYawHead = prevHeadYaw;
            }
        }

        if (entity instanceof EntityLivingBase livingMount) {
            livingMount.renderYawOffset = renderYawOffset;
            livingMount.prevRenderYawOffset = prevRenderYawOffset;
        }
    }
}
