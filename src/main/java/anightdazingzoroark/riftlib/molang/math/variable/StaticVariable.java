package anightdazingzoroark.riftlib.molang.math.variable;

/**
 * StaticVariables are immutable values that are shared across all
 * instances of IAnimatable and in particle emitters.
 * */
public class StaticVariable extends AbstractVariable {
    public StaticVariable(String name, double value) {
        super(name, value);
    }

    //block setting in staticvariable
    @Override
    public void set(double value) {}
}
