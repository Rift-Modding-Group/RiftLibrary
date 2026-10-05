package anightdazingzoroark.riftlib.molang.math;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public interface IValue {
    double get();

    @Nullable
    default Object getValue() {
        return this.get();
    }

    @NotNull
    default String getString() {
        return String.valueOf(this.getValue());
    }
}
