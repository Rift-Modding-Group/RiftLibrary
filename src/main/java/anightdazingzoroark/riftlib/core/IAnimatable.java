package anightdazingzoroark.riftlib.core;

import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This interface must be applied to any object that wants to be animated
 */
public interface IAnimatable<D extends AbstractAnimationData<?>> {
    D getAnimationData();

    /**
     * This registers animation controllers
     * */
    List<AnimationController<?, D>> createAnimationControllers();

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
     *
     */
    default Map<String, AnimatableRunValue> animationMessageEffects() {
        return Map.of();
    }
}
