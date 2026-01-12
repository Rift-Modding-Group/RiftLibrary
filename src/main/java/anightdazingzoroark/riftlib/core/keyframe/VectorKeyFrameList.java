package anightdazingzoroark.riftlib.core.keyframe;

import anightdazingzoroark.riftlib.core.util.Axis;
import anightdazingzoroark.riftlib.molang.MolangParser;

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
    public AnimationPoint getAnimationPointAtTick(MolangParser parser, double tick, Axis axis) {
        KeyFrameLocation location = this.getCurrentKeyFrameLocation(tick);
        KeyFrame currentFrame = location.currentFrame;

        KeyFrame.KeyFrameAxisValue startValueUnparsed = currentFrame.getStartValue().getValueFromAxis(axis);
        KeyFrame.KeyFrameAxisValue endValueUnparsed = currentFrame.getEndValue().getValueFromAxis(axis);

        double startValue = 0;
        double endValue = 0;

        try {
            startValue = startValueUnparsed.isExpression() ?
                    parser.parseExpression(startValueUnparsed.getExpressionValue()).get() : startValueUnparsed.getConstValue();
            endValue = endValueUnparsed.isExpression() ?
                    parser.parseExpression(endValueUnparsed.getExpressionValue()).get() : endValueUnparsed.getConstValue();
        }
        catch (Exception e) {}

        if (this.isRotation) {
            if (currentFrame.getStartValue().getValueFromAxis(axis).isExpression()) {
                startValue = Math.toRadians(startValue);
                if (axis == Axis.X || axis == Axis.Y) {
                    startValue *= -1;
                }
            }
            if (currentFrame.getEndValue().getValueFromAxis(axis).isExpression()) {
                endValue = Math.toRadians(endValue);
                if (axis == Axis.X || axis == Axis.Y) {
                    endValue *= -1;
                }
            }
        }

        return new AnimationPoint(currentFrame, location.currentTick, currentFrame.getLength(), startValue, endValue);
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
        return new KeyFrameLocation(this.keyFrames.get(this.keyFrames.size() - 1), tick);
    }
}
