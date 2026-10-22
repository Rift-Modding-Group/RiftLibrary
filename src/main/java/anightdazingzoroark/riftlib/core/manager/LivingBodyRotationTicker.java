package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

/**
 * Keeps living model bodies aligned with steering after vanilla body helpers have run.
 * */
public class LivingBodyRotationTicker {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWorldTick(@NotNull TickEvent.WorldTickEvent event) {
        if (event.side.isClient() || event.phase != TickEvent.Phase.END) return;
        event.world.getLoadedEntityList().forEach(LivingBodyRotationTicker::updateBodyRotation);
    }

    public static void updateBodyRotation(@NotNull Entity entity) {
        if (!(entity instanceof EntityLivingBase livingEntity)
                || !(entity instanceof IAnimatable<?> animatable)
                || !(animatable.getAnimationData() instanceof AnimationDataEntityLiving animationData)
                || !entity.isEntityAlive() || entity.isRiding() || entity.isBeingRidden()) return;

        if (animationData.applyServerBodyRotation()) return;

        boolean hasMovementRequest = false;
        if (livingEntity instanceof EntityLiving mob) {
            if (mob.isAIDisabled()) return;
            hasMovementRequest = !mob.getNavigator().noPath() || mob.getMoveHelper().isUpdating();
        }

        double displacementX = entity.posX - entity.prevPosX;
        double displacementZ = entity.posZ - entity.prevPosZ;
        boolean moving = displacementX * displacementX + displacementZ * displacementZ > 2.5E-7D;
        boolean steering = Math.abs(MathHelper.wrapDegrees(entity.rotationYaw - entity.prevRotationYaw)) > 1E-4f;
        // Client copies do not run the server's navigator or movement helper.
        // Their position and steering yaw still carry the movement heading.
        if (moving || steering || hasMovementRequest) livingEntity.renderYawOffset = entity.rotationYaw;
    }

    @SideOnly(Side.CLIENT)
    public static class Client {
        private static long bodyRotationTicks;

        public static double getBodyRotationTime(float partialTicks) {
            return bodyRotationTicks + (double) partialTicks;
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public void onRenderTick(@NotNull TickEvent.RenderTickEvent event) {
            if (event.phase != TickEvent.Phase.START) return;
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft.world == null || minecraft.isGamePaused()) return;
            minecraft.world.getLoadedEntityList().forEach(entity -> {
                if (entity instanceof IAnimatable<?> animatable
                        && animatable.getAnimationData() instanceof AnimationDataEntityLiving animationData) {
                    animationData.prepareBodyRotationFrame(event.renderTickTime);
                }
            });
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public void onClientTick(@NotNull TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft.world == null || minecraft.isGamePaused()) return;
            bodyRotationTicks++;
            minecraft.world.getLoadedEntityList().forEach(LivingBodyRotationTicker::updateBodyRotation);
        }
    }
}
