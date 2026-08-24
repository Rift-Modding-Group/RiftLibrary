package anightdazingzoroark.riftlib.core;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.processor.AnimationProcessor;
import anightdazingzoroark.riftlib.core.processor.IBone;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IAnimatableModel<E> {
	void setClientAnimations(E entity);

	AnimationProcessor getAnimationProcessor();

	@Nullable
	Animation getAnimations(@NotNull String name, IAnimatable<?> animatable);

	/**
	 * Gets a bone by name.
	 *
	 * @param boneName The bone name
	 * @return the bone
	 */
	default IBone getBone(String boneName) {
		RiftLib.LOGGER.warn("Cannot find bone {}.", boneName);
		return null;
	}
}
