package anightdazingzoroark.riftlib.ridePositionLogic;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.model.provider.GeoModelProvider;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityLivingRenderer;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityNonLivingRenderer;
import anightdazingzoroark.riftlib.util.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
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
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

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
            Entity passenger = event.getEntity();
            if (!(passenger.getRidingEntity() instanceof IDynamicRideUser<?> dynamicRideUser)) return;

            Entity dynamicRideEntity = dynamicRideUser.getDynamicRideUser();
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

        @SubscribeEvent
        public void onRenderWorldLast(@NotNull RenderWorldLastEvent event) {
            Minecraft minecraft = Minecraft.getMinecraft();
            Entity camera = minecraft.getRenderViewEntity();

            //draw the camera mount if its origin chunk section was skipped by normal rendering
            if (camera != null && camera.getRidingEntity() instanceof IDynamicRideUser<?> rideUser) {
                Entity mount = rideUser.getDynamicRideUser();
                DynamicRidePosSnapshot snapshot = rideUser.ridePosList().snapshot;
                Render<?> renderer = minecraft.getRenderManager().getEntityRenderObject(mount);
                if (!mount.isDead && mount.world == minecraft.world
                        && (renderer instanceof GeoEntityLivingRenderer<?> || renderer instanceof GeoEntityNonLivingRenderer<?>)
                        && snapshot.isClientAnimationPrepared(mount.world.getTotalWorldTime(), event.getPartialTicks())
                ) {
                    //an unused camera pose means neither renderer attempted this mount in the current view
                    //draw it before restoring the rider position so the model and camera use the same origin
                    boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
                    GlStateManager.pushMatrix();
                    GlStateManager.enableFog();
                    minecraft.entityRenderer.enableLightmap();
                    RenderHelper.enableStandardItemLighting();
                    minecraft.getRenderManager().renderEntityStatic(mount, event.getPartialTicks(), false);
                    RenderHelper.disableStandardItemLighting();
                    minecraft.entityRenderer.disableLightmap();
                    if (!fog) GlStateManager.disableFog();
                    GlStateManager.popMatrix();
                }
            }

            //restore the camera entity after the world finishes rendering.
            if (this.cameraRestoreEntity != null) {
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
}
