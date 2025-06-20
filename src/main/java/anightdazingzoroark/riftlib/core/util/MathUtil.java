package anightdazingzoroark.riftlib.core.util;

import java.util.function.Function;

import anightdazingzoroark.riftlib.core.easing.EasingManager;
import anightdazingzoroark.riftlib.core.easing.EasingType;
import anightdazingzoroark.riftlib.core.keyframe.AnimationPoint;

public class MathUtil {
	/**
	 * Lerps an AnimationPoint
	 *
	 * @param animationPoint The animation point
	 * @return the resulting lerped value
	 */
	public static float lerpValues(AnimationPoint animationPoint, EasingType easingType,
			Function<Double, Double> customEasingMethod) {
		if (animationPoint.currentTick >= animationPoint.animationEndTick) {
			return animationPoint.animationEndValue.floatValue();
		}
		if (animationPoint.currentTick == 0 && animationPoint.animationEndTick == 0) {
			return animationPoint.animationEndValue.floatValue();
		}

		if (easingType == EasingType.CUSTOM && customEasingMethod != null) {
			return lerpValues(customEasingMethod.apply(animationPoint.currentTick / animationPoint.animationEndTick),
					animationPoint.animationStartValue, animationPoint.animationEndValue);
		} else if (easingType == EasingType.NONE && animationPoint.keyframe != null) {
			easingType = animationPoint.keyframe.easingType;
		}
		double ease = EasingManager.ease(animationPoint.currentTick / animationPoint.animationEndTick, easingType,
				animationPoint.keyframe == null ? null : animationPoint.keyframe.easingArgs);
		return lerpValues(ease, animationPoint.animationStartValue, animationPoint.animationEndValue);
	}

	/**
	 * This is the actual function that smoothly interpolates (lerp) between
	 * keyframes
	 *
	 * @param startValue The animation's start value
	 * @param endValue   The animation's end value
	 * @return The interpolated value
	 */
	public static float lerpValues(double percentCompleted, double startValue, double endValue) {
		// current tick / position should be between 0 and 1 and represent the
		// percentage of the lerping that has completed
		return (float) lerp(percentCompleted, startValue, endValue);
	}

	public static double lerp(double pct, double start, double end) {
		return start + pct * (end - start);
	}
}
