package anightdazingzoroark.riftlib.molang.math;

import org.jetbrains.annotations.NotNull;

public class Constant implements IValue {
    private double value;

    public Constant(double value) {
        this.value = value;
    }

    @Override
    public double get() {
        return this.value;
    }

    public void set(double value) {
        this.value = value;
    }

    @Override
    @NotNull
    public String toString() {
        return String.valueOf(this.value);
    }
}
