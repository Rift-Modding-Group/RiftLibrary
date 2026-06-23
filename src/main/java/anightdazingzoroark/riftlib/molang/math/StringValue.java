package anightdazingzoroark.riftlib.molang.math;

public class StringValue implements IValue {
    private final String value;

    public StringValue(java.lang.String value) {
        this.value = value;
    }

    @Override
    public double get() {
        throw new IllegalStateException("String value cannot be used as a number: " + this.value);
    }

    @Override
    public String getString() {
        return this.value;
    }

    @Override
    public String toString() {
        return "'" + this.value.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }
}
