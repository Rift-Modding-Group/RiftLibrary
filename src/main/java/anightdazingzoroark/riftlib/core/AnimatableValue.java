package anightdazingzoroark.riftlib.core;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class AnimatableValue {
    private String variableName;
    private Double constantValue;
    private String expressionValue;
    private final boolean isExpression;

    public AnimatableValue(String variableName, Double constantValue) {
        this.variableName = variableName;
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

    public ImmutablePair<String, Double> getConstantValue() {
        return new ImmutablePair<>(this.variableName, this.constantValue);
    }

    public String getExpressionValue() {
        return this.expressionValue;
    }
}
