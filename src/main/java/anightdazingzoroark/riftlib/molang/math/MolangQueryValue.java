package anightdazingzoroark.riftlib.molang.math;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.molang.MolangObject;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MolangQueryValue implements IValue {
    @NotNull
    private final MolangParser parser;
    @NotNull
    private final String name;
    @NotNull
    private final IValue[] arguments;
    @Nullable
    private final AbstractAnimationData<?, ?> animationData;

    public MolangQueryValue(
            @NotNull MolangParser parser, @NotNull String name, @NotNull IValue[] arguments,
            @Nullable AbstractAnimationData<?, ?> animationData
    ) {
        this.parser = parser;
        this.name = name;
        this.arguments = arguments;
        this.animationData = animationData;
    }

    @Override
    public double get() {
        Object result = this.getValue();
        return result instanceof Number number ? number.doubleValue() : 0D;
    }

    @Override
    @Nullable
    public Object getValue() {
        MolangScope scope = this.parser.scope();
        MolangObject object = scope == null ? null : scope.getOwner();
        //query.actor_owner is registered here since... its not attached to any IAnimatable
        if (this.name.equals("query.actor_owner")) {
            return object == null ? null : object.getMolangActorOwner();
        }

        AbstractAnimationData<?, ?> targetData = object == null ? this.animationData : object.getMolangAnimationData();
        if (targetData == null) return 0D;

        MolangFunction function = targetData.getMolangQueries().get(this.name);
        if (function == null || this.arguments.length < function.getRequiredArgumentCount()) return 0D;
        return function.invoke(this.arguments, targetData);
    }

    @Override
    @NotNull
    public String toString() {
        if (this.arguments.length == 0) return this.name;
        return this.name + "(" + Arrays.stream(this.arguments).map(Object::toString).collect(Collectors.joining(", ")) + ")";
    }
}
