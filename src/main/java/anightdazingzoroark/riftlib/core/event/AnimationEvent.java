package anightdazingzoroark.riftlib.core.event;

import java.util.List;
import java.util.stream.Collectors;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import anightdazingzoroark.riftlib.util.MiscUtils;
import net.minecraft.entity.Entity;

public class AnimationEvent<T extends IAnimatable> {
	private final T animatable;
	public double animationTick;
	private final float partialTick;
	private final List<Object> extraData;
	protected AnimationController controller;

	public AnimationEvent(T animatable, float partialTick, List<Object> extraData) {
		this.animatable = animatable;
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

	public T getAnimatable() {
		return this.animatable;
	}

	public float getPartialTick() {
		return this.partialTick;
	}

	public boolean isMoving() {
		//a projectile not on the ground is usually on the move, whether its from gravity or from following
		//a trajectory
		if (this.animatable instanceof RiftLibProjectile projectile) return !projectile.onGround;
		else if (this.animatable instanceof Entity entity) return MiscUtils.getEntitySpeed(entity) > 0;
		return false;
	}

	public AnimationController getController() {
		return controller;
	}

	public void setController(AnimationController controller) {
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
