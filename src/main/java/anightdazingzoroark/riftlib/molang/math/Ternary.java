package anightdazingzoroark.riftlib.molang.math;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Ternary implements IValue {
    @NotNull
    private final IValue condition;
    @NotNull
    private final IValue ifTrue;
    @NotNull
    private final IValue ifFalse;

    public Ternary(@NotNull IValue condition, @NotNull IValue ifTrue, @NotNull IValue ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public double get() {
        Object value = this.getValue();
        return value instanceof Number number ? number.doubleValue() : 0D;
    }

    @Override
    @Nullable
    public Object getValue() {
        return this.condition.get() != 0D ? this.ifTrue.getValue() : this.ifFalse.getValue();
    }

    @Override
    @NotNull
    public String toString() {
        return this.condition + " ? " + this.ifTrue + " : " + this.ifFalse;
    }
}
