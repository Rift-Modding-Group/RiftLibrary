package anightdazingzoroark.riftlib.molang.expressions;

import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MolangValue extends MolangExpression {
    @NotNull
    private final IValue value;
    private boolean returns;

    public MolangValue(@NotNull IValue value) {
        this.value = value;
    }

    @NotNull
    public MolangValue addReturn() {
        this.returns = true;
        return this;
    }

    @Override
    public double get() {
        return this.value.get();
    }

    @Override
    @Nullable
    public Object getValue() {
        return this.value.getValue();
    }

    @NotNull
    public IValue getExpressionValue() {
        return this.value;
    }

    public boolean returns() {
        return this.returns;
    }

    @Override
    @NotNull
    public String toString() {
        return (this.returns ? "return " : "") + this.value;
    }

    @Override
    @NotNull
    public JsonElement toJson() {
        return this.value instanceof Constant ? new JsonPrimitive(this.value.get()) : super.toJson();
    }
}
