package anightdazingzoroark.riftlib.molang.math;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * This reinterpreted function class is going to be more or less a template.
 * Its results are to be then put in an anonymous IValue instance.
 */
public abstract class Function {
    @NotNull
    public final String name;

    public Function(@NotNull String name) {
        this.name = name;
    }

    public abstract int requiredArgCount();

    @NotNull
    public abstract BiFunction<IValue[], AbstractAnimationData<?, ?>, Double> operation();
}
