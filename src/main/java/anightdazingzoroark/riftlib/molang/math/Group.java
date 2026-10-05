package anightdazingzoroark.riftlib.molang.math;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Group implements IValue {
    @NotNull
    private final IValue value;

    public Group(@NotNull IValue value) {
        this.value = value;
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

    @Override
    @NotNull
    public String toString() {
        return "(" + this.value + ")";
    }
}
