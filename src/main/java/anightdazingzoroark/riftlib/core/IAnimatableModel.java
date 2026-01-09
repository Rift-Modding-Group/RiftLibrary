package anightdazingzoroark.riftlib.core;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.processor.AnimationProcessor;
import anightdazingzoroark.riftlib.core.processor.IBone;

public interface IAnimatableModel<E> {
	default double getCurrentTick() {
		return (System.nanoTime() / 1000000L / 50.0);
	}

	default void setLivingAnimations(E entity, Integer uniqueID) {
		this.setLivingAnimations(entity, uniqueID, null);
	}

	void setLivingAnimations(E entity, Integer uniqueID, AnimationEvent customPredicate);

	AnimationProcessor getAnimationProcessor();

	Animation getAnimation(String name, IAnimatable animatable);

	/**
	 * Gets a bone by name.
	 *
	 * @param boneName The bone name
	 * @return the bone
	 */
	default IBone getBone(String boneName) {
		IBone bone = this.getAnimationProcessor().getBone(boneName);
	    //todo: maybe add to config omitting errors from here
		if (bone == null) RiftLib.LOGGER.warn("Cannot find bone {}.", boneName);
		return bone;
	}

	void setMolangQueries(IAnimatable animatable, double currentTick);
}
