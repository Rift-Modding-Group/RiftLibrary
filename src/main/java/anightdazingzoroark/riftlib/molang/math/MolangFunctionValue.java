package anightdazingzoroark.riftlib.molang.math;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MolangFunctionValue implements IValue {
    @NotNull
    private final MolangFunction function;
    @NotNull
    private final IValue[] arguments;
    @Nullable
    private final AbstractAnimationData<?, ?> animationData;

    public MolangFunctionValue(@NotNull MolangFunction function, @NotNull IValue[] arguments, @Nullable AbstractAnimationData<?, ?> animationData) {
        this.function = function;
        this.arguments = arguments;
        this.animationData = animationData;
    }

    @Override
    public double get() {
        Object value = this.getValue();
        return value instanceof Number number ? number.doubleValue() : 0D;
    }

    @Override
    @Nullable
    public Object getValue() {
        return this.function.invoke(this.arguments, this.animationData);
    }

    @Override
    @NotNull
    public String toString() {
        return this.function.getName() + "(" + Arrays.stream(this.arguments).map(Object::toString).collect(Collectors.joining(", ")) + ")";
    }
}
