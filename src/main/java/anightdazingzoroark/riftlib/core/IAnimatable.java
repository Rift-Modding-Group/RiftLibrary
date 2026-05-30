package anightdazingzoroark.riftlib.core;

import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This interface must be applied to any object that wants to be animated
 */
public interface IAnimatable<D extends AbstractAnimationData<?, D>> {
    /**
     * The animation data for the object that will be animated.
     * */
    D getAnimationData();

    /**
     * Operations relevant to the initialization of animation data
     * are to be run here, such as animation controllers, molang
     * variable initialization, etc.
     * */
    void initializeAnimationData(D animationData);
}
