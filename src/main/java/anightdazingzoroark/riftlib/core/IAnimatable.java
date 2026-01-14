/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */
package anightdazingzoroark.riftlib.core;

import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This interface must be applied to any object that wants to be animated
 */
public interface IAnimatable {
	void registerControllers(AnimationData data);

	AnimationFactory getFactory();

	//this will be pretty important when it comes to hitbox and creature model scaling
	default float scale() {
		return 1f;
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
     * There might be times where the user would want their custom variable to
     * update on specific events that tickAnimationVariables() is not suited
     * for, or maybe their custom operation is also not suited for use there too,
     * hence, this method.
     */
    default void updateAnimationVariable(AnimatableValue value) {}
}
