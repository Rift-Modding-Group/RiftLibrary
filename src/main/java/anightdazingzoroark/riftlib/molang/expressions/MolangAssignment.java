package anightdazingzoroark.riftlib.molang.expressions;

import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Variable;

public class MolangAssignment extends MolangExpression {
    public final Variable variable;
    public final IValue expression;

    public MolangAssignment(MolangParser context, Variable variable, IValue expression) {
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
