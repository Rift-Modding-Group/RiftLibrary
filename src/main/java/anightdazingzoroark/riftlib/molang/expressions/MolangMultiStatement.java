package anightdazingzoroark.riftlib.molang.expressions;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Variable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MolangMultiStatement extends MolangExpression {
    @NotNull
    public final List<MolangExpression> expressions = new ArrayList<>();
    @NotNull
    public final Map<String, Variable> locals = new HashMap<>();

    public MolangMultiStatement(MolangParser context) {
        super(context);
    }

    public double get() {
        double value = 0;

        for (MolangExpression expression : this.expressions) {
            value = expression.get();
        }

        return value;
    }

    public String toString() {
        StringJoiner builder = new StringJoiner("; ");

        for (MolangExpression expression : this.expressions) {
            builder.add(expression.toString());
            if (expression instanceof MolangValue && ((MolangValue)expression).returns) {
                break;
            }
        }

        return builder.toString();
    }
}
