package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.internalMessage.RiftLibCreateOrDestroyRay;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Helper class for ray creation and destruction.
 * */
public class RiftLibRayHelper {
    /**
     * Creates a ray on both sides, takes into account if called from client or server too.
     * */
    public static void createRay(IRayCreator<?> rayCreator, String rayName) {
        if (rayCreator.getRayCreator().world.isRemote) {
            createRayOnSide(rayCreator, rayName);
            ServerProxy.RAY_MESSAGE_WRAPPER.sendToServer(new RiftLibCreateOrDestroyRay(true, rayCreator, rayName));
        }
        else {
            ServerProxy.RAY_MESSAGE_WRAPPER.sendToAll(new RiftLibCreateOrDestroyRay(true, rayCreator, rayName));
            createRayOnSide(rayCreator, rayName);
        }
    }

    /**
     * Create a ray on the side it was called on only.
     * */
    public static void createRayOnSide(IRayCreator<?> rayCreator, String rayName) {
        RiftLibRay.Builder rayBuilder = rayCreator.getRayBuilders().get(rayName);
        RiftLibRay ray = new RiftLibRay(
                rayBuilder.rayCreator,
                rayName,
                rayBuilder.parentLocatorName,
                rayBuilder.getRayType(),
                rayBuilder.getRayMaxLength(),
                rayBuilder.getRayWidthRange(),
                rayBuilder.getRaySpeed(),
                rayBuilder.getSpreadOnHitBlock(),
                rayBuilder.getBreakBlockCondition()
        );

        ImmutablePair<IRayCreator<?>, RiftLibRay> pairToAdd = new ImmutablePair<>(rayCreator, ray);
        if (rayCreator.getRayCreator().world.isRemote) RayTicker.Client.RAY_PAIR_LIST.add(pairToAdd);
        else RayTicker.Server.RAY_PAIR_LIST.add(pairToAdd);
    }

    /**
     * Works on both sides, kills a ray on the client side. The ray will fade out then die on both sides.
     * */
    public static void killRay(IRayCreator<?> rayCreator, @NotNull String rayName) {
        if (rayCreator.getRayCreator().world.isRemote) {
            killRayOnSide(rayCreator, rayName);
            ServerProxy.RAY_MESSAGE_WRAPPER.sendToServer(new RiftLibCreateOrDestroyRay(false, rayCreator, rayName));
        }
        else {
            ServerProxy.RAY_MESSAGE_WRAPPER.sendToAll(new RiftLibCreateOrDestroyRay(false, rayCreator, rayName));
            killRayOnSide(rayCreator, rayName);
        }
    }

    /**
     * Kill a ray on the side it was called on only.
     * */
    public static void killRayOnSide(IRayCreator<?> rayCreator, @NotNull String rayName) {
        List<ImmutablePair<IRayCreator<?>, RiftLibRay>> listToKillOn = rayCreator.getRayCreator().world.isRemote ? RayTicker.Client.RAY_PAIR_LIST : RayTicker.Server.RAY_PAIR_LIST;

        for (ImmutablePair<IRayCreator<?>, RiftLibRay> rayPair : listToKillOn) {
            if (rayCreator == rayPair.getLeft() && rayName.equals(rayPair.getRight().rayName)) {
                rayPair.getRight().endRay();
            }
        }
    }
}
