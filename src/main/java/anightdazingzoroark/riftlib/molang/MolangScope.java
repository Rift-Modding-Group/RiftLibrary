package anightdazingzoroark.riftlib.molang;

import java.util.HashMap;
import java.util.Map;

public final class MolangScope {
    private final MolangScope parent;
    private final Map<String, Double> values = new HashMap<>();

    public MolangScope() {
        this(null);
    }

    public MolangScope(MolangScope parent) {
        this.parent = parent;
    }

    public double get(String name) {
        Double v = this.values.get(name);
        if (v != null) return v;
        return this.parent != null ? this.parent.get(name) : 0.0;
    }

    public void set(String name, double value) {
        this.values.put(name, value);
    }
}
