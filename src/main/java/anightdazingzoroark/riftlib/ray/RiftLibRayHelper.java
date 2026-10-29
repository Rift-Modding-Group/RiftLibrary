package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationDataAdvancedEntity;
import anightdazingzoroark.riftlib.internalMessage.RiftLibCreateOrDestroyRay;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Helper class for ray creation and destruction.
 * */
public class RiftLibRayHelper {
    /**
     * Creates a ray. Meant for use on server only.
     * */
    @Nullable
    public static <T extends Entity & IAnimatable<? extends AbstractAnimationDataAdvancedEntity<?, ?>>> RiftLibRay createRay(
            @NotNull RiftLibRayBuilder rayBuilder, @NotNull String locatorName, @NotNull T rayCreator
    ) {
        if (rayCreator.world.isRemote) return null;
        RiftLibRay ray = createRayOnSide(rayBuilder, locatorName, rayCreator);
        if (ray == null) return null;

        ServerProxy.RAY_MESSAGE_WRAPPER.sendToAllTracking(
                new RiftLibCreateOrDestroyRay(true, rayCreator, rayBuilder.getName(), locatorName),
                rayCreator
        );
        return ray;
    }

    /**
     * Create a ray on the side it was called on only.
     * */
    @Nullable
    public static RiftLibRay createRayOnSide(
            @NotNull RiftLibRayBuilder rayBuilder, @NotNull String locatorName, @NotNull Entity rayCreator
    ) {
        if (!(rayCreator instanceof IAnimatable<?> animatable
                && animatable.getAnimationData() instanceof AbstractAnimationDataAdvancedEntity<?, ?> animData)) {
            RiftLib.LOGGER.warn("Cannot create ray {} on an entity without advanced animation data!", rayBuilder.getName());
            return null;
        }
        //test validity
        if (!rayBuilder.isValid()) {
            RiftLib.LOGGER.warn("Ray {} is invalid, and thus will not form.", rayBuilder.getName());
            return null;
        }

        //ensure theres a server model
        if (!rayCreator.world.isRemote) {
            ServerModelRegistry.requireServerModel(animatable, "rays");
        }
        ServerModelRegistry.prepareServerSyncedAnimationData(animatable);

        //ensure theres a locator
        AnimatedLocator locator = animData.getAnimatedLocator(locatorName);
        if (locator == null) {
            RiftLib.LOGGER.warn("Given locator {} does not exist on the entity!", locatorName);
            return null;
        }

        RiftLibRay ray = new RiftLibRay(rayCreator, animData, locator, rayBuilder);
        if (rayCreator.world.isRemote) RayTicker.Client.RAY_LIST.add(ray);
        else RayTicker.Server.RAY_LIST.add(ray);
        return ray;
    }

    /**
     * Kills a ray.  Meant for use on server only.
     * */
    public static void killRay(@NotNull Entity rayCreator, @NotNull String rayName) {
        if (rayCreator.world.isRemote) return;
        ServerProxy.RAY_MESSAGE_WRAPPER.sendToAllTracking(
                new RiftLibCreateOrDestroyRay(false, rayCreator, rayName, ""),
                rayCreator
        );
        killRayOnSide(rayCreator, rayName);
    }

    /**
     * Kill a ray on the side it was called on only.
     * */
    public static void killRayOnSide(@NotNull Entity rayCreator, @NotNull String rayName) {
        List<RiftLibRay> listToKillOn = rayCreator.world.isRemote ? RayTicker.Client.RAY_LIST : RayTicker.Server.RAY_LIST;

        for (RiftLibRay ray : listToKillOn) {
            if (!rayCreator.equals(ray.rayCreator) || !rayName.equals(ray.rayName)) continue;
            ray.endRay();
        }
    }
}
