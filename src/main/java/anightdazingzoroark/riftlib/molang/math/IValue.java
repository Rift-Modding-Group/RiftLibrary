package anightdazingzoroark.riftlib.molang.math;

public interface IValue {
    double get();

    default String getString() {
        return String.valueOf(this.get());
    }
}
