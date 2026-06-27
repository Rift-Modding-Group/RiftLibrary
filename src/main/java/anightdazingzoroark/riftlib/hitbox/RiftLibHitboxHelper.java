package anightdazingzoroark.riftlib.hitbox;

import net.minecraft.world.World;

public class RiftLibHitboxHelper {
    private static int id;

    public static void createOffenseHitbox(IMultiHitboxUser<?> multiHitboxUser, String hitboxName) {
        World world = multiHitboxUser.getWorld();
        if (world.isRemote) {}
        else createOffenseHitboxOnSide(multiHitboxUser, hitboxName);
    }

    public static void createOffenseHitboxOnSide(IMultiHitboxUser<?> multiHitboxUser, String hitboxName) {}
}
