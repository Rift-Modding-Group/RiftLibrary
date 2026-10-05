package anightdazingzoroark.riftlib.molang.expressions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.StringJoiner;

public class MolangMultiStatement extends MolangExpression {
    @NotNull
    private final List<MolangExpression> expressions;

    public MolangMultiStatement(@NotNull List<MolangExpression> expressions) {
        this.expressions = List.copyOf(expressions);
    }

    @Override
    public double get() {
        Object value = this.getValue();
        return value instanceof Number number ? number.doubleValue() : 0D;
    }

    @Override
    @Nullable
    public Object getValue() {
        Object value = 0D;

        for (MolangExpression expression : this.expressions) {
            value = expression.getValue();
            if (expression instanceof MolangValue molangValue && molangValue.returns()) break;
        }

        return value;
    }

    @NotNull
    public List<MolangExpression> getExpressions() {
        return this.expressions;
    }

    @Override
    @NotNull
    public String toString() {
        StringJoiner builder = new StringJoiner("; ");

        for (MolangExpression expression : this.expressions) {
            builder.add(expression.toString());
            if (expression instanceof MolangValue molangValue && molangValue.returns()) break;
        }

        return builder.toString();
    }
}
