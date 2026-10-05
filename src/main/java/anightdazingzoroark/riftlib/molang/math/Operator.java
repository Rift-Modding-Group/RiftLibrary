package anightdazingzoroark.riftlib.molang.math;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Operator implements IValue {
    @NotNull
    private final Operation operation;
    @NotNull
    private final IValue left;
    @NotNull
    private final IValue right;

    public Operator(@NotNull Operation operation, @NotNull IValue left, @NotNull IValue right) {
        this.operation = operation;
        this.left = left;
        this.right = right;
    }

    @Override
    public double get() {
        return switch (this.operation) {
            case NULL_COALESCING -> {
                Object value = this.getValue();
                yield value instanceof Number number ? number.doubleValue() : 0D;
            }
            case AND -> this.left.get() != 0D && this.right.get() != 0D ? 1D : 0D;
            case OR -> this.left.get() != 0D || this.right.get() != 0D ? 1D : 0D;
            case EQUALS -> this.valuesEqual() ? 1D : 0D;
            case NOT_EQUALS -> this.valuesEqual() ? 0D : 1D;
            default -> this.operation.calculate(this.left.get(), this.right.get());
        };
    }

    @Override
    @Nullable
    public Object getValue() {
        if (this.operation == Operation.NULL_COALESCING) {
            Object value = this.left.getValue();
            return value != null ? value : this.right.getValue();
        }
        return this.get();
    }

    private boolean valuesEqual() {
        Object leftValue = this.left.getValue();
        Object rightValue = this.right.getValue();
        if (leftValue instanceof Number leftNumber && rightValue instanceof Number rightNumber) {
            return Operation.equals(leftNumber.doubleValue(), rightNumber.doubleValue());
        }
        return Objects.equals(leftValue, rightValue);
    }

    @Override
    @NotNull
    public String toString() {
        return this.left + " " + this.operation.getSign() + " " + this.right;
    }
}
