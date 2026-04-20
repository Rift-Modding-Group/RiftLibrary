package anightdazingzoroark.riftlib.core.event;

import java.util.List;
import java.util.stream.Collectors;

import anightdazingzoroark.riftlib.core.controller.AnimationController;

public class AnimationEvent {
	public double animationTick;
	private final float partialTick;
	private final List<Object> extraData;
	protected AnimationController<?> controller;

	public AnimationEvent(float partialTick, List<Object> extraData) {
		this.partialTick = partialTick;
		this.extraData = extraData;
	}

	/**
	 * Gets the amount of ticks that have passed in either the current transition or
	 * animation, depending on the controller's AnimationState.
	 *
	 * @return the animation tick
	 */
	public double getAnimationTick() {
		return this.animationTick;
	}

	public float getPartialTick() {
		return this.partialTick;
	}

	public AnimationController<?> getController() {
		return controller;
	}

	public void setController(AnimationController<?> controller) {
		this.controller = controller;
	}

	public List<Object> getExtraData() {
		return extraData;
	}

	@SuppressWarnings("hiding")
	public <T> List<T> getExtraDataOfType(Class<T> type) {
		return extraData.stream().filter(x -> type.isAssignableFrom(x.getClass())).map(x -> type.cast(x))
				.collect(Collectors.toList());
	}
}
