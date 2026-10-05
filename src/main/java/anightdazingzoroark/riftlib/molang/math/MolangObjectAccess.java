package anightdazingzoroark.riftlib.molang.math;

import anightdazingzoroark.riftlib.molang.MolangObject;
import anightdazingzoroark.riftlib.molang.MolangParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MolangObjectAccess implements IValue {
    @NotNull
    private final MolangParser parser;
    @NotNull
    private final IValue object;
    @NotNull
    private final IValue value;

    public MolangObjectAccess(@NotNull MolangParser parser, @NotNull IValue object, @NotNull IValue value) {
        this.parser = parser;
        this.object = object;
        this.value = value;
    }

    @Override
    public double get() {
        Object result = this.getValue();
        return result instanceof Number number ? number.doubleValue() : 0D;
    }

    @Override
    @Nullable
    public Object getValue() {
        Object target = this.object.getValue();
        if (!(target instanceof MolangObject molangObject)) return 0D;
        return this.parser.withScope(molangObject.getMolangScope(), this.value::getValue);
    }

    public boolean canSetValue() {
        return this.value instanceof Variable;
    }

    public void setValue(@Nullable Object newValue) {
        Object target = this.object.getValue();
        if (!(target instanceof MolangObject molangObject) || !(this.value instanceof Variable variable)) return;
        this.parser.withScope(molangObject.getMolangScope(), () -> variable.setValue(newValue));
    }

    @Override
    @NotNull
    public String toString() {
        return this.object + " -> " + this.value;
    }
}
