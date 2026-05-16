package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.internalMessage.RiftLibCreateOrDestroyRay;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class for ray creation.
 * */
public class RiftLibRayHelper {
    /**
     * Works on both sides, creates a ray on the client side.
     * */
    public static void createRay(IRayCreator<?> rayCreator, String rayName) {
        if (rayCreator.getRayCreator().world.isRemote) {
            RiftLibRay.Builder rayBuilder = rayCreator.getRays().get(rayName);
            RiftLibRay ray = new RiftLibRay(
                    rayBuilder.rayCreator(),
                    rayName,
                    rayBuilder.parentLocatorName(),
                    rayBuilder.maxRayLength(),
                    rayBuilder.rayWidth(),
                    rayBuilder.rayCreationTime(),
                    rayBuilder.rayFadeOutTime()
            );
            RayTicker.RAY_PAIR_LIST.add(new ImmutablePair<>(rayCreator, ray));
        }
        else ServerProxy.RAY_MESSAGE_WRAPPER.sendToAll(new RiftLibCreateOrDestroyRay(true, rayCreator, rayName));
    }
    /**
     * Works on both sides, kills a ray. The ray will fade out then die.
     * */
    public static void killRay(IRayCreator<?> rayCreator, @NotNull String rayName) {
        if (rayCreator.getRayCreator().world.isRemote) {
            for (ImmutablePair<IRayCreator<?>, RiftLibRay> rayPair : RayTicker.RAY_PAIR_LIST) {
                if (rayCreator == rayPair.getLeft() && rayName.equals(rayPair.getRight().rayName)) {
                    rayPair.getRight().endRay();
                }
            }
        }
        else ServerProxy.RAY_MESSAGE_WRAPPER.sendToAll(new RiftLibCreateOrDestroyRay(false, rayCreator, rayName));
    }
}
