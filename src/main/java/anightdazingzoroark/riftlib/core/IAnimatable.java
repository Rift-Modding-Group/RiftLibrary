/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */
package anightdazingzoroark.riftlib.core;

import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;

import java.util.HashMap;
import java.util.Map;

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
     * @return A Map of Strings and AnimatableValues. The string is to be the name of the
     * variable, while the AnimatableValue is to be its initial starting value
     */
    default Map<String, AnimatableValue> createAnimationVariables() {
        return new HashMap<>();
    }

    /**
     * This runs as long as the rendered object gets rendered
     *
     * @return A Map of Strings and AnimatableValues. The string is to be the name of the
     * variable, while the AnimatableValue is to be the new value
     */
    default Map<String, AnimatableValue> updateAnimationVariables() {
        return new HashMap<>();
    }
}
