package anightdazingzoroark.riftlib.molang;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.molang.expressions.MolangAssignment;
import anightdazingzoroark.riftlib.molang.expressions.MolangExpression;
import anightdazingzoroark.riftlib.molang.expressions.MolangMultiStatement;
import anightdazingzoroark.riftlib.molang.expressions.MolangValue;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.math.MathBuilder;
import anightdazingzoroark.riftlib.molang.math.MolangObjectAccess;
import anightdazingzoroark.riftlib.molang.math.MolangQueryValue;
import anightdazingzoroark.riftlib.molang.math.Operation;
import anightdazingzoroark.riftlib.molang.math.Variable;
import anightdazingzoroark.riftlib.util.MolangUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MolangParser extends MathBuilder {
    @NotNull
    public static final MolangExpression ZERO = new MolangValue(new Constant(0D));
    @NotNull
    public static final MolangExpression ONE = new MolangValue(new Constant(1D));
    @NotNull
    public static final String RETURN = "return ";

    @NotNull
    private final ScopedValue<MolangScope> currentScope = ScopedValue.newInstance();

    public MolangParser() {
        this.registerFunction("function.send_message", 1, (arguments, animationData) -> {
            if (animationData == null) return 0D;
            return MolangUtils.booleanToDouble(animationData.sendMessage(arguments[0].getString()));
        });
        this.registerFunction("function.create_offense_hitbox_by_name", 1, (arguments, animationData) -> {
            if (animationData == null || animationData.getWorld() == null || animationData.getWorld().isRemote) return 0D;
            if (!(animationData.getHolder() instanceof IMultiHitboxUser<?> multiHitboxUser)) return 0D;
            return MolangUtils.booleanToDouble(multiHitboxUser.getMultiHitboxList().createOffenseHitboxByName(arguments[0].getString()));
        });
        this.registerFunction("function.destroy_offense_hitbox_by_name", 1, (arguments, animationData) -> {
            if (animationData == null || animationData.getWorld() == null || animationData.getWorld().isRemote) return 0D;
            if (!(animationData.getHolder() instanceof IMultiHitboxUser<?> multiHitboxUser)) return 0D;
            return MolangUtils.booleanToDouble(multiHitboxUser.getMultiHitboxList().removeOffenseHitboxByName(arguments[0].getString()));
        });
        this.registerFunction("function.create_offense_hitbox_by_tag", 1, (arguments, animationData) -> {
            if (animationData == null || animationData.getWorld() == null || animationData.getWorld().isRemote) return 0D;
            if (!(animationData.getHolder() instanceof IMultiHitboxUser<?> multiHitboxUser)) return 0D;
            return MolangUtils.booleanToDouble(multiHitboxUser.getMultiHitboxList().createOffenseHitboxesByTag(arguments[0].getString()));
        });
        this.registerFunction("function.destroy_offense_hitbox_by_tag", 1, (arguments, animationData) -> {
            if (animationData == null || animationData.getWorld() == null || animationData.getWorld().isRemote) return 0D;
            if (!(animationData.getHolder() instanceof IMultiHitboxUser<?> multiHitboxUser)) return 0D;
            return MolangUtils.booleanToDouble(multiHitboxUser.getMultiHitboxList().removeOffenseHitboxesByTag(arguments[0].getString()));
        });
    }

    @Nullable
    public MolangScope scope() {
        return this.currentScope.isBound() ? this.currentScope.get() : null;
    }

    public void withScope(@NotNull MolangScope scope, @NotNull Runnable operation) {
        ScopedValue.where(this.currentScope, scope).run(operation);
    }

    @Nullable
    public <T> T withScope(@NotNull MolangScope scope, @NotNull Supplier<T> operation) {
        return ScopedValue.where(this.currentScope, scope).call(operation::get);
    }

    public void setVariable(@NotNull String name, double value) {
        this.getVariable(name).set(value);
    }

    @Override
    @NotNull
    public Variable getVariable(@NotNull String name) {
        Variable variable = super.getVariable(name);
        if (variable != null) return variable;

        variable = new Variable(this, name);
        this.registerVariable(variable);
        return variable;
    }

    @NotNull
    public MolangExpression parseExpression(@NotNull String expression) throws MolangException {
        return this.parseExpression(expression, null);
    }

    @NotNull
    public MolangExpression parseExpression(@NotNull String expression, @Nullable AbstractAnimationData<?, ?> animationData) throws MolangException {
        String normalized = this.lowercaseOutsideStrings(expression.trim());
        List<String> statements = new ArrayList<>();
        StringBuilder statement = new StringBuilder();
        boolean inString = false;
        boolean escaping = false;
        char quote = 0;

        for (int i = 0; i < normalized.length(); i++) {
            char character = normalized.charAt(i);
            if (inString) {
                statement.append(character);
                if (escaping) escaping = false;
                else if (character == '\\') escaping = true;
                else if (character == quote) inString = false;
            }
            else if (character == '\'' || character == '"') {
                inString = true;
                quote = character;
                statement.append(character);
            }
            else if (character == ';') {
                if (!statement.toString().isBlank()) statements.add(statement.toString());
                statement.setLength(0);
            }
            else statement.append(character);
        }

        if (inString) throw new MolangException("Unterminated string literal in '" + expression + "'!");
        if (!statement.toString().isBlank()) statements.add(statement.toString());
        if (statements.isEmpty()) throw new MolangException("Molang expression cannot be blank!");

        List<MolangExpression> parsedStatements = new ArrayList<>(statements.size());
        for (String currentStatement : statements) {
            parsedStatements.add(this.parseOneLine(currentStatement, animationData));
        }
        return new MolangMultiStatement(parsedStatements);
    }

    @NotNull
    protected MolangExpression parseOneLine(@NotNull String expression, @Nullable AbstractAnimationData<?, ?> animationData) throws MolangException {
        String trimmed = expression.trim();
        if (trimmed.startsWith(RETURN)) {
            List<Token> returnTokens = this.tokenize(trimmed.substring(RETURN.length()));
            return new MolangValue(this.parseTokens(returnTokens, animationData)).addReturn();
        }

        List<Token> tokens = this.tokenize(trimmed);
        int assignmentIndex = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getType() != TokenType.ASSIGNMENT) continue;
            if (assignmentIndex >= 0) throw new MolangException("A statement can only contain one assignment!");
            assignmentIndex = i;
        }
        if (assignmentIndex >= 0) {
            if (assignmentIndex == 0 || assignmentIndex == tokens.size() - 1) {
                throw new MolangException("Assignment is missing a target or value!");
            }
            if (assignmentIndex == 1 && tokens.getFirst().getType() == TokenType.IDENTIFIER
                    && this.isFunction(tokens.getFirst().getText())) {
                throw new MolangException("Cannot assign a value to function '" + tokens.getFirst().getText() + "'!");
            }

            IValue target = this.parseTokens(tokens.subList(0, assignmentIndex), animationData);
            IValue value = this.parseTokens(tokens.subList(assignmentIndex + 1, tokens.size()), animationData);
            if (target instanceof Variable variable) return new MolangAssignment(variable, value);
            if (target instanceof MolangObjectAccess objectAccess && objectAccess.canSetValue()) {
                return new MolangAssignment(objectAccess, value);
            }
            throw new MolangException("The left side of an assignment must be a variable!");
        }
        return new MolangValue(this.parseTokens(tokens, animationData));
    }

    @Override
    @NotNull
    protected IValue createOperator(@NotNull Operation operation, @NotNull IValue left, @NotNull IValue right) throws MolangException {
        if (operation == Operation.ARROW) return new MolangObjectAccess(this, left, right);
        return super.createOperator(operation, left, right);
    }

    @Override
    @NotNull
    protected IValue createQuery(@NotNull String name, @NotNull IValue[] arguments, @Nullable AbstractAnimationData<?, ?> animationData) {
        return new MolangQueryValue(this, name, arguments, animationData);
    }
}
