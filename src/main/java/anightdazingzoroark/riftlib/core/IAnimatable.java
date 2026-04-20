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

    /**
     * This registers animation controllers
     * */
    void registerControllers(T data);

    /**
     * This only runs once and will run when this object just started rendering
     * This is meant for updating molang variables once
     *
     * @return A List of AnimatableValues.
     */
    default List<AnimatableValue> createAnimationVariables() {
        return new ArrayList<>();
    }

    /**
     * This runs as long as the rendered object gets rendered
     * This is meant for updating molang variables repeatedly
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
