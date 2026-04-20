package anightdazingzoroark.riftlib.core;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This interface must be applied to any object that wants to be animated
 */
public interface IAnimatable<T extends AbstractAnimationData<?>> {
    T getAnimationData();

    void registerControllers(T data);

    default void initializeAnimationData() {
        T data = this.getAnimationData();
        if (data != null) data.initialize();
    }

    /**
     * This only runs once and will run when this object just started rendering
     *
     * @return A List of AnimatableValues.
     */
    default List<AnimatableValue> createAnimationVariables() {
        return new ArrayList<>();
    }

    /**
     * This runs as long as the rendered object gets rendered
     *
     * @return A List of AnimatableValues.
     */
    default List<AnimatableValue> tickAnimationVariables() {
        return new ArrayList<>();
    }

    /**
     * This allows for running custom code from animations on the server
     * */
    default HashMap<String, Runnable> animationMessageEffects() {
        return new HashMap<>();
    }
}
