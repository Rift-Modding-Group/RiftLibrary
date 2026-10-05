package anightdazingzoroark.riftlib.molang.math;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Variable implements IValue {
    @NotNull
    private final MolangParser parser;
    @NotNull
    private final String name;

    public Variable(@NotNull MolangParser parser, @NotNull String name) {
        this.parser = parser;
        this.name = name;
    }

    public void set(double value) {
        MolangScope scope = this.parser.scope();
        if (scope != null) scope.set(this.name, value);
    }

    public void setValue(@Nullable Object value) {
        MolangScope scope = this.parser.scope();
        if (scope != null) scope.setValue(this.name, value);
    }

    @Override
    public double get() {
        MolangScope scope = this.parser.scope();
        return scope != null ? scope.get(this.name) : 0D;
    }

    @Override
    @Nullable
    public Object getValue() {
        MolangScope scope = this.parser.scope();
        return scope != null ? scope.getValue(this.name) : null;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @Override
    @NotNull
    public String toString() {
        return this.name;
    }
}
