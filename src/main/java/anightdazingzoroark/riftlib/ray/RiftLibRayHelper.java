package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.internalMessage.RiftLibCreateOrDestroyRay;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for ray creation.
 * */
public class RiftLibRayHelper {
    /**
     * Works on both sides, creates a ray on the client side.
     * */
    public static void createRay(IRayCreator<?> entity, String rayName) {
        if (entity.getRayCreator().world.isRemote) {
            RiftLibRay ray = entity.getRays().get(rayName);
            RayTicker.RAY_PAIR_LIST.add(new ImmutablePair<>(entity, ray));
        }
        else ServerProxy.RAY_MESSAGE_WRAPPER.sendToAll(new RiftLibCreateOrDestroyRay(true, entity, rayName));
    }
    /**
     * Works on both sides, kills a ray. The ray will fade out then die.
     * */
    public static void killRay(IRayCreator<?> entity, @NotNull String rayName) {
        if (entity.getRayCreator().world.isRemote) {
            for (ImmutablePair<IRayCreator<?>, RiftLibRay> rayPair : RayTicker.RAY_PAIR_LIST) {
                RiftLibRay ray = entity.getRays().get(rayName);
                if (entity == rayPair.getLeft() && ray == rayPair.getRight()) {
                    ray.endRay();
                    break;
                }
            }
        }
        else ServerProxy.RAY_MESSAGE_WRAPPER.sendToAll(new RiftLibCreateOrDestroyRay(false, entity, rayName));
    }
}
