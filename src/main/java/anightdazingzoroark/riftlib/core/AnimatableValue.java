package anightdazingzoroark.riftlib.core;

public class AnimatableValue {
    private double constantValue;
    private String expressionValue;
    private final boolean isExpression;

    public AnimatableValue(double constantValue) {
        this.constantValue = constantValue;
        this.isExpression = false;
    }

    public AnimatableValue(String expressionValue) {
        this.expressionValue = expressionValue;
        this.isExpression = true;
    }

    public boolean isExpression() {
        return this.isExpression;
    }

    public double getConstantValue() {
        return this.constantValue;
    }

    public String getExpressionValue() {
        return this.expressionValue;
    }
}
