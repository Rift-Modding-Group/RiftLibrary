package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.internalMessage.RiftLibCreateOrDestroyRay;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Helper class for ray creation and destruction.
 * */
public class RiftLibRayHelper {
    /**
     * Creates a ray. Meant for use on server only.
     * */
    public static void createRay(IRayCreator<?> rayCreator, String rayName, String locatorName) {
        if (rayCreator.getRayCreator().world.isRemote) return;
        ServerProxy.RAY_MESSAGE_WRAPPER.sendToAllTracking(
                new RiftLibCreateOrDestroyRay(true, rayCreator, rayName, locatorName),
                rayCreator.getRayCreator()
        );
        createRayOnSide(rayCreator, rayName, locatorName);
    }

    /**
     * Create a ray on the side it was called on only.
     * */
    public static void createRayOnSide(IRayCreator<?> rayCreator, String rayName, String locatorName) {
        RiftLibRayBuilder rayBuilder = rayCreator.getRayBuilders().get(rayName);

        //test validity
        if (!rayBuilder.isValid()) {
            RiftLib.LOGGER.warn("This ray is invalid, and thus will not form.");
            return;
        }

        //ensure theres a server model
        ServerModelRegistry.requireServerModel(rayCreator.getRayCreator(), "rays");
        ServerModelRegistry.prepareServerSyncedAnimationData(rayCreator.getRayCreator());

        //ensure theres a locator
        AnimatedLocator locator = rayCreator.getRayCreator().getAnimationData().getAnimatedLocator(locatorName);
        if (locator == null) {
            RiftLib.LOGGER.warn("Given locator {} does not exist on the entity!", locatorName);
            return;
        }

        RiftLibRay ray = new RiftLibRay(rayCreator, rayName, locator, rayBuilder);
        if (rayCreator.getRayCreator().world.isRemote) RayTicker.Client.RAY_LIST.add(ray);
        else RayTicker.Server.RAY_LIST.add(ray);
    }

    /**
     * Kills a ray.  Meant for use on server only.
     * */
    public static void killRay(IRayCreator<?> rayCreator, @NotNull String rayName) {
        if (rayCreator.getRayCreator().world.isRemote) return;
        ServerProxy.RAY_MESSAGE_WRAPPER.sendToAllTracking(
                new RiftLibCreateOrDestroyRay(false, rayCreator, rayName, ""),
                rayCreator.getRayCreator()
        );
        killRayOnSide(rayCreator, rayName);
    }

    /**
     * Kill a ray on the side it was called on only.
     * */
    public static void killRayOnSide(IRayCreator<?> rayCreator, @NotNull String rayName) {
        List<RiftLibRay> listToKillOn = rayCreator.getRayCreator().world.isRemote ? RayTicker.Client.RAY_LIST : RayTicker.Server.RAY_LIST;

        for (RiftLibRay ray : listToKillOn) {
            if (!rayCreator.equals(ray.rayCreator) || !rayName.equals(ray.rayName)) continue;
            ray.endRay();
        }
    }
}
