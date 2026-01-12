package anightdazingzoroark.riftlib.core.keyframe;

import anightdazingzoroark.riftlib.core.easing.EasingType;
import anightdazingzoroark.riftlib.core.util.Axis;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KeyFrame {
    private Double length;
    private KeyFrameVectorValue startValue;
    private KeyFrameVectorValue endValue;
    private final EasingType easingType;
    private final List<Double> easingArgs;

    public KeyFrame(Double length, KeyFrameVectorValue startValue, KeyFrameVectorValue endValue) {
        this(length, startValue, endValue, EasingType.Linear);
    }

    public KeyFrame(Double length, KeyFrameVectorValue startValue, KeyFrameVectorValue endValue, EasingType easingType) {
        this(length, startValue, endValue, easingType, new ArrayList<>());
    }

    public KeyFrame(Double length, KeyFrameVectorValue startValue, KeyFrameVectorValue endValue, EasingType easingType, List<Double> easingArgs) {
        this.length = length;
        this.startValue = startValue;
        this.endValue = endValue;
        this.easingType = easingType;
        this.easingArgs = easingArgs;
    }

    public Double getLength() {
        return this.length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public KeyFrameVectorValue getStartValue() {
        return this.startValue;
    }

    public void setStartValue(KeyFrameVectorValue startValue) {
        this.startValue = startValue;
    }

    public KeyFrameVectorValue getEndValue() {
        return this.endValue;
    }

    public void setEndValue(KeyFrameVectorValue endValue) {
        this.endValue = endValue;
    }

    public EasingType getEasingType() {
        return this.easingType;
    }

    public List<Double> getEasingArgs() {
        return this.easingArgs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.length, this.startValue, this.endValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof KeyFrame)) return false;
        KeyFrame objKeyFrameValue = (KeyFrame) obj;
        return this.hashCode() == objKeyFrameValue.hashCode();
    }

    /**
     * This class is meant for storing a value for a keyframe for a specific axis
     */
    public static class KeyFrameAxisValue {
        private final boolean isExpression;
        private Double constValue;
        private String expressionValue;

        public KeyFrameAxisValue(Double value) {
            this.constValue = value;
            this.isExpression = false;
        }

        public KeyFrameAxisValue(String value) {
            this.expressionValue = value;
            this.isExpression = true;
        }

        public boolean isExpression() {
            return this.isExpression;
        }

        public Double getConstValue() {
            return this.constValue;
        }

        public String getExpressionValue() {
            return this.expressionValue;
        }
    }

    /**
     * This class is meant for storing a vector of KeyFrameAxisValues
     */
    public static class KeyFrameVectorValue {
        private final KeyFrameAxisValue xAxisValue;
        private final KeyFrameAxisValue yAxisValue;
        private final KeyFrameAxisValue zAxisValue;

        public KeyFrameVectorValue(KeyFrameAxisValue xAxisValue, KeyFrameAxisValue yAxisValue, KeyFrameAxisValue zAxisValue) {
            this.xAxisValue = xAxisValue;
            this.yAxisValue = yAxisValue;
            this.zAxisValue = zAxisValue;
        }

        public KeyFrameAxisValue getValueFromAxis(Axis axis) {
            switch (axis) {
                case X:
                    return this.xAxisValue;
                case Y:
                    return this.yAxisValue;
                case Z:
                    return this.zAxisValue;
            }
            return null;
        }
    }
}
