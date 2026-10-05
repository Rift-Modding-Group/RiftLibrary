package anightdazingzoroark.riftlib.molang.expressions;

import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.math.Operation;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

public abstract class MolangExpression implements IValue {
    public static boolean isZero(@NotNull MolangExpression expression) {
        return isConstant(expression, 0f);
    }

    public static boolean isOne(@NotNull MolangExpression expression) {
        return isConstant(expression, 1f);
    }

    public static boolean isConstant(@NotNull MolangExpression expression, double x) {
        if (expression instanceof MolangValue value) {
            return value.getExpressionValue() instanceof Constant && Operation.equals(value.getExpressionValue().get(), x);
        }
        return false;
    }

    public static boolean isExpressionConstant(@NotNull MolangExpression expression) {
        if (expression instanceof MolangValue value) {
            return value.getExpressionValue() instanceof Constant;
        }
        return false;
    }

    @NotNull
    public JsonElement toJson() {
        return new JsonPrimitive(this.toString());
    }
}
