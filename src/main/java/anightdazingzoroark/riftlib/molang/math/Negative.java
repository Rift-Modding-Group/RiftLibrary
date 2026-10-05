package anightdazingzoroark.riftlib.molang.math;

import org.jetbrains.annotations.NotNull;

public class Negative implements IValue {
    @NotNull
    private final IValue value;

    public Negative(@NotNull IValue value) {
        this.value = value;
    }

    @Override
    public double get() {
        return -this.value.get();
    }

    @Override
    @NotNull
    public String toString() {
        return "-" + this.value;
    }
}
