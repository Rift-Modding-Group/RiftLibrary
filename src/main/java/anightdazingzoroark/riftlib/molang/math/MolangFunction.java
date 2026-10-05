package anightdazingzoroark.riftlib.molang.math;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class MolangFunction {
    @NotNull
    private final String name;
    private final int requiredArgumentCount;
    @NotNull
    private final BiFunction<IValue[], AbstractAnimationData<?, ?>, ?> operation;

    public MolangFunction(@NotNull String name, int requiredArgumentCount,
                          @NotNull BiFunction<IValue[], AbstractAnimationData<?, ?>, ?> operation) {
        this.name = name;
        this.requiredArgumentCount = requiredArgumentCount;
        this.operation = operation;
    }

    @Nullable
    public Object invoke(@NotNull IValue[] arguments, @Nullable AbstractAnimationData<?, ?> animationData) {
        return this.operation.apply(arguments, animationData);
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    public int getRequiredArgumentCount() {
        return this.requiredArgumentCount;
    }
}
