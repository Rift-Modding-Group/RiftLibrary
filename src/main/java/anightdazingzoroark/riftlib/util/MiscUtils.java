package anightdazingzoroark.riftlib.util;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class MiscUtils {
    //better than using the motion variables :tm:
    public static double getEntityHorizontalSpeed(@NotNull Entity entity) {
        return Math.hypot(entity.posX - entity.prevPosX, entity.posZ - entity.prevPosZ);
    }

    public static double getEntityVerticalSpeed(@NotNull Entity entity) {
        return entity.posY - entity.prevPosY;
    }
}