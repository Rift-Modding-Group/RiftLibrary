package anightdazingzoroark.riftlib.molang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MolangScope {
    @Nullable
    private final MolangScope parent;
    @Nullable
    private final MolangObject owner;
    @NotNull
    private final Map<String, Object> values = new HashMap<>();

    public MolangScope() {
        this(null, null);
    }

    public MolangScope(@Nullable MolangScope parent, @Nullable MolangObject owner) {
        this.parent = parent;
        this.owner = owner;
    }

    public double get(@NotNull String name) {
        Object value = this.getValue(name);
        return value instanceof Number number ? number.doubleValue() : 0D;
    }

    @Nullable
    public Object getValue(@NotNull String name) {
        if (this.values.containsKey(name)) return this.values.get(name);
        return this.parent != null ? this.parent.getValue(name) : null;
    }

    public void set(@NotNull String name, double value) {
        this.values.put(name, value);
    }

    public void setValue(@NotNull String name, @Nullable Object value) {
        this.values.put(name, value);
    }

    @Nullable
    public MolangObject getOwner() {
        return this.owner;
    }

    @NotNull
    public Map<String, Double> getValues() {
        Map<String, Double> numericValues = new HashMap<>();
        for (Map.Entry<String, Object> entry : this.values.entrySet()) {
            if (entry.getValue() instanceof Number number) {
                numericValues.put(entry.getKey(), number.doubleValue());
            }
        }
        return Collections.unmodifiableMap(numericValues);
    }

    public void replaceValues(@NotNull Map<String, ?> values) {
        this.values.clear();
        this.values.putAll(values);
    }
}
