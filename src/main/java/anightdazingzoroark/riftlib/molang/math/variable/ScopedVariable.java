package anightdazingzoroark.riftlib.molang.math.variable;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;

/**
 * StaticVariables are mutable values that, despite being stored in the same
 * parser, through multithreading, are unique among each IAnimatable, particle
 * emitter, and particle.
 * */
public class ScopedVariable extends AbstractVariable {
    private final MolangParser parser;

    public ScopedVariable(MolangParser parser, String name, double value) {
        super(name, value);
        this.parser = parser;
    }

    @Override
    public void set(double value) {
        MolangScope scope = this.parser.scope();
        if (scope != null) scope.set(getName(), value);
    }

    @Override
    public double get() {
        MolangScope scope = this.parser.scope();
        return scope != null ? scope.get(getName()) : 0.0;
    }
}
