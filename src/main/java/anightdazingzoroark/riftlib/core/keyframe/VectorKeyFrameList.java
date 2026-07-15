package anightdazingzoroark.riftlib.core.keyframe;

import anightdazingzoroark.riftlib.core.ExpressionValue;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.core.util.Axis;

import java.util.ArrayList;
import java.util.List;

public class VectorKeyFrameList {
    private final List<KeyFrame> keyFrames = new ArrayList<>();
    private final boolean isRotation;

    public VectorKeyFrameList() {
        this(false);
    }

    public VectorKeyFrameList(boolean isRotation) {
        this.isRotation = isRotation;
    }

    public void addKeyFrame(KeyFrame keyFrame) {
        this.keyFrames.add(keyFrame);
    }

    public boolean isRotation() {
        return this.isRotation;
    }

    public double getLastKeyframeTime() {
        double toReturn = 0;
        for (KeyFrame keyFrame : this.keyFrames) {
            toReturn += keyFrame.getLength();
        }
        return toReturn;
    }

    public boolean isEmpty() {
        return this.keyFrames.isEmpty();
    }

    // Helper method to transform a KeyFrameLocation to an AnimationPoint
    public AnimationPoint getAnimationPointAtTick(AbstractAnimationData<?, ?> animData, double tick, Axis axis) {
        KeyFrameLocation location = this.getCurrentKeyFrameLocation(tick);
        KeyFrame currentFrame = location.currentFrame;

        ExpressionValue startExpressionValue = currentFrame.getStartValue().getValueFromAxis(axis);
        ExpressionValue endExpressionValue = currentFrame.getEndValue().getValueFromAxis(axis);

        double startValue = this.isRotation
                ? this.convertRotationValueToRadians(startExpressionValue, animData, axis)
                : startExpressionValue.get(animData);
        double endValue = this.isRotation
                ? this.convertRotationValueToRadians(endExpressionValue, animData, axis)
                : endExpressionValue.get(animData);

        return new AnimationPoint(currentFrame, location.currentTick, currentFrame.getLength(), startValue, endValue);
    }

    private double convertRotationValueToRadians(ExpressionValue value, AbstractAnimationData<?, ?> animData, Axis axis) {
        double converted = Math.toRadians(value.get(animData));
        if ((axis == Axis.X || axis == Axis.Y) && !value.isExpression()) {
            converted *= -1;
        }
        return converted;
    }

    /**
     * Returns the current keyframe object, plus how long the previous keyframes
     * have taken (aka elapsed animation time)
     **/
    private KeyFrameLocation getCurrentKeyFrameLocation(double tick) {
        double totalTimeTracker = 0;
        for (KeyFrame frame : this.keyFrames) {
            totalTimeTracker += frame.getLength();
            if (totalTimeTracker > tick) {
                double tickToTest = (tick - (totalTimeTracker - frame.getLength()));
                return new KeyFrameLocation(frame, tickToTest);
            }
        }
        return new KeyFrameLocation(this.keyFrames.getLast(), tick);
    }
}
