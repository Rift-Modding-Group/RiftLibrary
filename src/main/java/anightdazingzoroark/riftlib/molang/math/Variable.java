package anightdazingzoroark.riftlib.molang.math;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;

public class Variable implements IValue {
    private final MolangParser parser;
    private final String name;

    public Variable(MolangParser parser, String name) {
        this.parser = parser;
        this.name = name;
    }

    public void set(double value) {
        MolangScope scope = this.parser.scope();
        if (scope != null) scope.set(this.name, value);
    }

    public double get() {
        MolangScope scope = this.parser.scope();
        return scope != null ? scope.get(this.name) : 0D;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }
}
