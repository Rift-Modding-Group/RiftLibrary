package anightdazingzoroark.riftlib.molang.math;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;

//todo: maybe replace variable with this
public class ScopedVariable extends Variable {
    private final MolangParser parser;

    public ScopedVariable(MolangParser parser, String name, double value) {
        super(name, value);
        this.parser = parser;
    }

    @Override
    public void set(double value) {
        MolangScope s = this.parser.scope();
        if (s != null) s.set(getName(), value);
    }

    @Override
    public double get() {
        MolangScope s = this.parser.scope();
        return s != null ? s.get(getName()) : 0.0;
    }
}
