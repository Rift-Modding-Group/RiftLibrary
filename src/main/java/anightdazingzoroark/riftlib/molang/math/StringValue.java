package anightdazingzoroark.riftlib.molang.math;

import org.jetbrains.annotations.NotNull;

public class StringValue implements IValue {
    @NotNull
    private final String value;

    public StringValue(@NotNull String value) {
        this.value = value;
    }

    @Override
    public double get() {
        throw new IllegalStateException("String value cannot be used as a number: " + this.value);
    }

    @Override
    @NotNull
    public String getValue() {
        return this.value;
    }

    @Override
    @NotNull
    public String getString() {
        return this.value;
    }

    @Override
    @NotNull
    public String toString() {
        return "'" + this.value.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }
}
