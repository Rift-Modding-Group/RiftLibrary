package anightdazingzoroark.riftlib.molang.math.variable;

import anightdazingzoroark.riftlib.molang.math.IValue;

public abstract class AbstractVariable implements IValue {
    private final String name;
    private double value;

    public AbstractVariable(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public void set(double value) {
        this.value = value;
    }

    public double get() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }
}
