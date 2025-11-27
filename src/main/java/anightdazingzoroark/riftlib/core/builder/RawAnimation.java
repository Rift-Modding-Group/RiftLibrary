/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package anightdazingzoroark.riftlib.core.builder;

import java.util.Objects;

public class RawAnimation {
	public String animationName;

	/**
	 * If loop is null, the animation processor will use the loopByDefault boolean
	 * to decide if the animation should loop.
	 */
	public final LoopType loopType;

	/**
	 * A raw animation only stores the animation name and if it should loop, nothing
	 * else
	 *
	 * @param animationName The name of the animation
	 * @param loop          Whether it should loop
	 */
	public RawAnimation(String animationName, LoopType loop) {
		this.animationName = animationName;
		this.loopType = loop;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof RawAnimation)) return false;

		RawAnimation animation = (RawAnimation) obj;
        return animation.loopType == this.loopType && animation.animationName.equals(this.animationName);
    }

	@Override
	public int hashCode() {
		return Objects.hash(animationName, loopType);
	}
}
