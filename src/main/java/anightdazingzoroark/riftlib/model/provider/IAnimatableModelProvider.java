package anightdazingzoroark.riftlib.model.provider;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IAnimatableModelProvider<E> {
	/**
	 * This resource location needs to point to a json file of your animation file,
	 * i.e. "geckolib:animations/frog_animation.json"
	 *
	 * @return the animation file location
	 */
	@NotNull
	List<String> getAnimationIdentifiers(E animatable);
}
