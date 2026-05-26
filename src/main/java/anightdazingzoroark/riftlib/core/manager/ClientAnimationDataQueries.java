package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
class ClientAnimationDataQueries {
    static double distanceFromCamera(Entity holder) {
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();

        if (camera == null) return 0D;

        Vec3d entityCamera = new Vec3d(
                Interpolations.lerp(camera.prevPosX, camera.posX, partialTick),
                Interpolations.lerp(camera.prevPosY, camera.posY, partialTick),
                Interpolations.lerp(camera.prevPosZ, camera.posZ, partialTick)
        );
        Vec3d entityPosition = new Vec3d(
                Interpolations.lerp(holder.prevPosX, holder.posX, partialTick),
                Interpolations.lerp(holder.prevPosY, holder.posY, partialTick),
                Interpolations.lerp(holder.prevPosZ, holder.posZ, partialTick)
        );
        return entityCamera.add(ActiveRenderInfo.getCameraPosition()).distanceTo(entityPosition);
    }

    static double yawSpeed(Entity holder) {
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
        float currentEntityYaw = Interpolations.lerpYaw(holder.prevRotationYaw, holder.rotationYaw, partialTick);
        float prevEntityYaw = Interpolations.lerpYaw(holder.prevRotationYaw, holder.rotationYaw, partialTick - 0.1f);
        return currentEntityYaw - prevEntityYaw;
    }
}
