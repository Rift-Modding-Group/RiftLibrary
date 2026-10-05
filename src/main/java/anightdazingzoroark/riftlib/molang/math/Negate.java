package anightdazingzoroark.riftlib.molang.math;

import org.jetbrains.annotations.NotNull;

public class Negate implements IValue {
    @NotNull
    private final IValue value;

    public Negate(@NotNull IValue value) {
        this.value = value;
    }

    @Override
    public double get() {
        return this.value.get() == 0D ? 1D : 0D;
    }

    @Override
    @NotNull
    public String toString() {
        return "!" + this.value;
    }
}
