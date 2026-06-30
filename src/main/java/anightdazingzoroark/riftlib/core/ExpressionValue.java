package anightdazingzoroark.riftlib.core;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.util.MolangUtils;

/**
 * Helper class that stores items that were either double or molang expression
 * in original json
 * */
public class ExpressionValue {
    private final boolean isExpression;
    private String expressionValue;
    private Double numericalValue;

    public ExpressionValue(RawMolangValue rawMolangValue) {
        if (rawMolangValue.stringValue != null) {
            this.isExpression = true;
            this.expressionValue = rawMolangValue.stringValue;
        }
        else {
            this.isExpression = false;
            this.numericalValue = rawMolangValue.numericalValue;
        }
    }

    public ExpressionValue(double value) {
        this.isExpression = false;
        this.numericalValue = value;
    }

    public double get(AbstractAnimationData<?, ?> animData) {
        if (this.isExpression) return MolangUtils.parseValueAndGet(animData, this.expressionValue);
        else return this.numericalValue;
    }

    public boolean isExpression() {
        return this.isExpression;
    }
}
