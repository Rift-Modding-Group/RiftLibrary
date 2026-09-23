package anightdazingzoroark.riftlib.ridePositionLogic;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.model.provider.GeoModelProvider;
import anightdazingzoroark.riftlib.util.AnimationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DynamicRidePosTicker {
    @SideOnly(Side.CLIENT)
    public static class Client {
        private Entity cameraRestoreEntity;
        private double cameraRestorePosX;
        private double cameraRestorePosY;
        private double cameraRestorePosZ;
        private double cameraRestorePrevPosX;
        private double cameraRestorePrevPosY;
        private double cameraRestorePrevPosZ;
        private double cameraRestoreLastTickPosX;
        private double cameraRestoreLastTickPosY;
        private double cameraRestoreLastTickPosZ;

        //cancel normal player rendering, the ridden entity renderer handles dynamic ride passengers.
        @SubscribeEvent
        public void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
            EntityPlayer player = event.getEntityPlayer();
            Entity ridingEntity = player.getRidingEntity();
            if (ridingEntity instanceof IDynamicRideUser<?> dynamicRideUser && !dynamicRideUser.ridePosList().snapshot.isRenderingPassenger(player)) {
                event.setCanceled(true);
            }
        }

        //cancel normal non-player passenger rendering for dynamic ride users.
        @SubscribeEvent
        public <T extends EntityLivingBase> void onLivingRenderPre(RenderLivingEvent.Pre<T> event) {
            EntityLivingBase passenger = event.getEntity();
            if (passenger instanceof EntityPlayer) return;

            Entity ridingEntity = passenger.getRidingEntity();
            if (ridingEntity instanceof IDynamicRideUser<?> dynamicRideUser && !dynamicRideUser.ridePosList().snapshot.isRenderingPassenger(passenger)) {
                event.setCanceled(true);
            }
        }

        //move the camera to the current dynamic ride position for this frame.
        @SubscribeEvent
        public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
            if (!(event.getEntity() instanceof EntityLivingBase passenger)) return;
            if (!(passenger.getRidingEntity() instanceof IDynamicRideUser<?> dynamicRideUser)) return;

            EntityLivingBase dynamicRideEntity = dynamicRideUser.getDynamicRideUser();
            DynamicRidePosSnapshot ridePosSnapshot = dynamicRideUser.ridePosList().snapshot;
            GeoModelProvider<?> modelProvider = AnimationUtils.getGeoModelForEntity(dynamicRideEntity);
            if (modelProvider instanceof AnimatedGeoModel<?> animatedGeoModel) {
                animatedGeoModel.prepareClientAnimationPose((IAnimatable<?>) dynamicRideEntity);
                ridePosSnapshot.markClientAnimationPrepared(
                        dynamicRideEntity.world.getTotalWorldTime(),
                        (float) event.getRenderPartialTicks()
                );
            }

            float partialTicks = (float) event.getRenderPartialTicks();
            ridePosSnapshot.storeSnapshot(partialTicks, dynamicRideUser.getRenderYaw(partialTicks));
            ridePosSnapshot.cachePassengerRidePositions();
            ridePosSnapshot.restoreSnapshot();
            Vec3d ridePos = ridePosSnapshot.getRidePosition(passenger);
            if (ridePos == null) return;

            if (this.cameraRestoreEntity == null) {
                this.cameraRestoreEntity = passenger;
                this.cameraRestorePosX = passenger.posX;
                this.cameraRestorePosY = passenger.posY;
                this.cameraRestorePosZ = passenger.posZ;
                this.cameraRestorePrevPosX = passenger.prevPosX;
                this.cameraRestorePrevPosY = passenger.prevPosY;
                this.cameraRestorePrevPosZ = passenger.prevPosZ;
                this.cameraRestoreLastTickPosX = passenger.lastTickPosX;
                this.cameraRestoreLastTickPosY = passenger.lastTickPosY;
                this.cameraRestoreLastTickPosZ = passenger.lastTickPosZ;
            }

            passenger.posX = ridePos.x;
            passenger.posY = ridePos.y;
            passenger.posZ = ridePos.z;
            passenger.prevPosX = passenger.posX;
            passenger.prevPosY = passenger.posY;
            passenger.prevPosZ = passenger.posZ;
            passenger.lastTickPosX = passenger.posX;
            passenger.lastTickPosY = passenger.posY;
            passenger.lastTickPosZ = passenger.posZ;
        }

        //restore the camera entity after the world finishes rendering.
        @SubscribeEvent
        public void onRenderWorldLast(RenderWorldLastEvent event) {
            if (this.cameraRestoreEntity == null) return;

            this.cameraRestoreEntity.posX = this.cameraRestorePosX;
            this.cameraRestoreEntity.posY = this.cameraRestorePosY;
            this.cameraRestoreEntity.posZ = this.cameraRestorePosZ;
            this.cameraRestoreEntity.prevPosX = this.cameraRestorePrevPosX;
            this.cameraRestoreEntity.prevPosY = this.cameraRestorePrevPosY;
            this.cameraRestoreEntity.prevPosZ = this.cameraRestorePrevPosZ;
            this.cameraRestoreEntity.lastTickPosX = this.cameraRestoreLastTickPosX;
            this.cameraRestoreEntity.lastTickPosY = this.cameraRestoreLastTickPosY;
            this.cameraRestoreEntity.lastTickPosZ = this.cameraRestoreLastTickPosZ;
            this.cameraRestoreEntity = null;
        }
    }
}
