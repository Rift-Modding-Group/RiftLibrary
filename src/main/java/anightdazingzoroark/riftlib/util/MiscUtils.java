package anightdazingzoroark.riftlib.util;

import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class MiscUtils {
    //better than using the motion variables :tm:
    public static double getEntityHorizontalSpeed(Entity entity) {
        float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();

        //query.modified_distance_moved is to be modifiable based on entity speed
        //multiplied by amount of ticks ever since they moved
        double currentPosX = Interpolations.lerp(entity.prevPosX, entity.posX, partialTicks);
        double currentPosZ = Interpolations.lerp(entity.prevPosZ, entity.posZ, partialTicks);

        double dx = currentPosX - entity.prevPosX;
        double dz = currentPosZ - entity.prevPosZ;

        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double getEntityVerticalSpeed(Entity entity) {
        float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
        double currentPosY = Interpolations.lerp(entity.prevPosY, entity.posY, partialTicks);
        return currentPosY - entity.prevPosY;
    }
}
