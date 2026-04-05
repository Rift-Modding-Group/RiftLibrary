package anightdazingzoroark.riftlib.molang.expressions;

import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.math.variable.AbstractVariable;
import anightdazingzoroark.riftlib.molang.MolangParser;

public class MolangAssignment extends MolangExpression {
    public AbstractVariable variable;
    public IValue expression;

    public MolangAssignment(MolangParser context, AbstractVariable variable, IValue expression) {
        super(context);
        this.variable = variable;
        this.expression = expression;
    }

    public double get() {
        double value = this.expression.get();
        this.variable.set(value);
        return value;
    }

    public String toString() {
        return this.variable.getName() + " = " + this.expression.toString();
    }
}
