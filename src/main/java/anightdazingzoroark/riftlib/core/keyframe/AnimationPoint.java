package anightdazingzoroark.riftlib.core.keyframe;

public class AnimationPoint {
    /**
     * The current tick in the animation to lerp from
     */
    public final Double currentTick;
    /**
     * The tick that the current animation should end at
     */
    public final Double animationEndTick;
    /**
     * The Animation start value.
     */
    public final Double animationStartValue;
    /**
     * The Animation end value.
     */
    public final Double animationEndValue;

    /**
     * The current keyframe.
     */

    public final KeyFrame keyframe;



    public AnimationPoint(KeyFrame keyframe, Double currentTick, Double animationEndTick, Double animationStartValue, Double animationEndValue) {
        this.keyframe = keyframe;
        this.currentTick = currentTick;
        this.animationEndTick = animationEndTick;
        this.animationStartValue = animationStartValue;
        this.animationEndValue = animationEndValue;
    }

    public AnimationPoint(KeyFrame keyframe, double tick, double animationEndTick, float animationStartValue, double animationEndValue) {
        this.keyframe = keyframe;
        this.currentTick = tick;
        this.animationEndTick = animationEndTick;
        this.animationStartValue = Double.valueOf(animationStartValue);
        this.animationEndValue = animationEndValue;
    }
}
