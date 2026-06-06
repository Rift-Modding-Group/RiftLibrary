package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.internalMessage.RiftLibCreateOrDestroyRay;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
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
    public static void createRay(IRayCreator<?> rayCreator, String rayName, String locatorName) {
        if (rayCreator.getRayCreator().world.isRemote) {
            createRayOnSide(rayCreator, rayName, locatorName);
            ServerProxy.RAY_MESSAGE_WRAPPER.sendToServer(new RiftLibCreateOrDestroyRay(true, rayCreator, rayName, locatorName));
        }
        else {
            ServerProxy.RAY_MESSAGE_WRAPPER.sendToAll(new RiftLibCreateOrDestroyRay(true, rayCreator, rayName, locatorName));
            createRayOnSide(rayCreator, rayName, locatorName);
        }
    }

    /**
     * Create a ray on the side it was called on only.
     * */
    public static void createRayOnSide(IRayCreator<?> rayCreator, String rayName, String locatorName) {
        RiftLibRay.Builder rayBuilder = rayCreator.getRayBuilders().get(rayName);

        //ensure theres a ray type
        if (rayBuilder.getRayType() == null) {
            RiftLib.LOGGER.warn("This ray has no ray type, and thus will not form.");
            return;
        }

        //ensure theres a server model
        ServerModelRegistry.requireServerModel(rayCreator.getRayCreator(), "rays");

        //ensure theres a locator
        AnimatedLocator locator = rayCreator.getRayCreator().getAnimationData().getAnimatedLocator(locatorName);
        if (locator == null) {
            RiftLib.LOGGER.warn("Given locator {} does not exist on the entity!", locatorName);
            return;
        }

        RiftLibRay ray = new RiftLibRay(
                rayBuilder.rayCreator,
                rayName,
                locator,
                rayBuilder.getRayType(),
                rayBuilder.getRayMaxLength(),
                rayBuilder.getRayWidthRange(),
                rayBuilder.getRaySpeed(),
                rayBuilder.getSpreadOnHitBlock(),
                rayBuilder.getOnlyOneSegment(),
                rayBuilder.getBreakBlockCondition()
        );

        ImmutablePair<IRayCreator<?>, RiftLibRay> pairToAdd = new ImmutablePair<>(rayCreator, ray);
        if (rayCreator.getRayCreator().world.isRemote) RayTicker.Client.RAY_PAIR_LIST.add(pairToAdd);
        else RayTicker.Server.RAY_PAIR_LIST.add(pairToAdd);
    }

    /**
     * Works on both sides, kills a ray.
     * */
    public static void killRay(IRayCreator<?> rayCreator, @NotNull String rayName) {
        if (rayCreator.getRayCreator().world.isRemote) {
            killRayOnSide(rayCreator, rayName);
            ServerProxy.RAY_MESSAGE_WRAPPER.sendToServer(new RiftLibCreateOrDestroyRay(false, rayCreator, rayName, ""));
        }
        else {
            ServerProxy.RAY_MESSAGE_WRAPPER.sendToAll(new RiftLibCreateOrDestroyRay(false, rayCreator, rayName, ""));
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
