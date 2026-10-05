package anightdazingzoroark.riftlib.molang.expressions;

import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.math.MolangObjectAccess;
import anightdazingzoroark.riftlib.molang.math.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MolangAssignment extends MolangExpression {
    @NotNull
    private final IValue target;
    @NotNull
    private final IValue expression;

    public MolangAssignment(@NotNull Variable variable, @NotNull IValue expression) {
        this.target = variable;
        this.expression = expression;
    }

    public MolangAssignment(@NotNull MolangObjectAccess objectAccess, @NotNull IValue expression) {
        this.target = objectAccess;
        this.expression = expression;
    }

    @Override
    public double get() {
        Object value = this.getValue();
        return value instanceof Number number ? number.doubleValue() : 0D;
    }

    @Override
    @Nullable
    public Object getValue() {
        Object value = this.expression.getValue();
        if (this.target instanceof Variable variable) variable.setValue(value);
        else if (this.target instanceof MolangObjectAccess objectAccess) objectAccess.setValue(value);
        return value;
    }

    @Override
    @NotNull
    public String toString() {
        return this.target + " = " + this.expression;
    }
}
