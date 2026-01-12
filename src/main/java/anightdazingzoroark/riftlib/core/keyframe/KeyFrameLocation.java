package anightdazingzoroark.riftlib.core.keyframe;

public class KeyFrameLocation {
    /**
     * The curent frame.
     */
    public KeyFrame currentFrame;

    /**
     * This is the combined total time of all the previous keyframes
     */
    public double currentTick;

    /**
     * Instantiates a new Key frame location.
     *
     * @param currentFrame the current frame
     * @param currentTick  the current animation tick
     */
    public KeyFrameLocation(KeyFrame currentFrame, double currentTick) {
        this.currentFrame = currentFrame;
        this.currentTick = currentTick;
    }
}
