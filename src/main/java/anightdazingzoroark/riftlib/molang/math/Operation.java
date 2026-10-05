package anightdazingzoroark.riftlib.molang.math;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Operation {
    NULL_COALESCING("??", 1),
    OR("||", 2),
    AND("&&", 3),
    EQUALS("==", 4),
    NOT_EQUALS("!=", 4),
    LESS("<", 5),
    LESS_THAN("<=", 5),
    GREATER(">", 5),
    GREATER_THAN(">=", 5),
    ADD("+", 6),
    SUB("-", 6),
    MUL("*", 7),
    DIV("/", 7),
    MOD("%", 7),
    POW("^", 8),
    ARROW("->", 9);

    @NotNull
    private final String sign;
    private final int precedence;

    Operation(@NotNull String sign, int precedence) {
        this.sign = sign;
        this.precedence = precedence;
    }

    public double calculate(double left, double right) {
        return switch (this) {
            case ADD -> left + right;
            case SUB -> left - right;
            case MUL -> left * right;
            case DIV -> left / (right == 0D ? 1D : right);
            case MOD -> left % right;
            case POW -> Math.pow(left, right);
            case AND -> left != 0D && right != 0D ? 1D : 0D;
            case OR -> left != 0D || right != 0D ? 1D : 0D;
            case LESS -> left < right ? 1D : 0D;
            case LESS_THAN -> left <= right ? 1D : 0D;
            case GREATER -> left > right ? 1D : 0D;
            case GREATER_THAN -> left >= right ? 1D : 0D;
            case EQUALS -> equals(left, right) ? 1D : 0D;
            case NOT_EQUALS -> !equals(left, right) ? 1D : 0D;
            case ARROW, NULL_COALESCING -> throw new UnsupportedOperationException(this.sign + " requires contextual evaluation");
        };
    }

    public static boolean equals(double left, double right) {
        return Math.abs(left - right) < 1.0E-5D;
    }

    @Nullable
    public static Operation fromSign(@NotNull String sign) {
        for (Operation operation : values()) {
            if (operation.sign.equals(sign)) return operation;
        }
        return null;
    }

    @NotNull
    public String getSign() {
        return this.sign;
    }

    public int getPrecedence() {
        return this.precedence;
    }

    public boolean isRightAssociative() {
        return this == POW || this == NULL_COALESCING;
    }
}
