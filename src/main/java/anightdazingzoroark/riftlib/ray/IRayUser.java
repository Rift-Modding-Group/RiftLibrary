package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;

import java.util.Map;

public interface IRayUser<D extends AbstractAnimationData<?>> {
    /**
     * All active rays created from the animatable. Key represents GeoLocator,
     * value represents well the ray to launch.
     * */
    Map<String, RiftLibRay> getRays();
}
