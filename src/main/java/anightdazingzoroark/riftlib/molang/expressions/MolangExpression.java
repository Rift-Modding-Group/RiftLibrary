package anightdazingzoroark.riftlib.molang.expressions;

import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.math.Operation;
import anightdazingzoroark.riftlib.molang.MolangParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public abstract class MolangExpression implements IValue {
    public MolangParser context;

    public static boolean isZero(MolangExpression expression) {
        return isConstant(expression, 0f);
    }

    public static boolean isOne(MolangExpression expression) {
        return isConstant(expression, 1f);
    }

    public static boolean isConstant(MolangExpression expression, double x) {
        if (expression instanceof MolangValue) {
            MolangValue value = (MolangValue)expression;
            return value.value instanceof Constant && Operation.equals(value.value.get(), x);
        }
        return false;
    }

    public static boolean isExpressionConstant(MolangExpression expression) {
        if (expression instanceof MolangValue) {
            MolangValue value = (MolangValue)expression;
            return value.value instanceof Constant;
        }
        return false;
    }

    public MolangExpression(MolangParser context) {
        this.context = context;
    }

    public JsonElement toJson() {
        return new JsonPrimitive(this.toString());
    }
}
