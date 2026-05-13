package anightdazingzoroark.riftlib.molang;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.molang.math.*;
import anightdazingzoroark.riftlib.molang.expressions.MolangAssignment;
import anightdazingzoroark.riftlib.molang.expressions.MolangExpression;
import anightdazingzoroark.riftlib.molang.expressions.MolangMultiStatement;
import anightdazingzoroark.riftlib.molang.expressions.MolangValue;
import anightdazingzoroark.riftlib.molang.math.Variable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class MolangParser extends MathBuilder {
    public static final MolangExpression ZERO = new MolangValue(null, new Constant(0f));
    public static final MolangExpression ONE = new MolangValue(null, new Constant(1f));
    public static final String RETURN = "return ";
    private final ThreadLocal<Deque<MolangScope>> scopeStack = ThreadLocal.withInitial(ArrayDeque::new);
    private MolangMultiStatement currentStatement;

    public MolangScope scope() {
        Deque<MolangScope> scope = this.scopeStack.get();
        return scope.isEmpty() ? null : scope.peek();
    }

    public void pushScope(MolangScope scope) {
        this.scopeStack.get().push(scope);
    }

    public void popScope() {
        this.scopeStack.get().pop();
    }

    public void withScope(MolangScope scope, Runnable r) {
        this.pushScope(scope);
        try {
            r.run();
        }
        finally {
            this.popScope();
        }
    }

    public void setValue(String name, double value) {
        Variable variable = this.getVariable(name);
        if (variable != null) variable.set(value);
    }

    public Variable getVariable(String name) {
        Variable variable = this.currentStatement == null ? null : this.currentStatement.locals.get(name);
        if (variable == null) variable = super.getVariable(name);

        if (variable == null) {
            variable = new Variable(this, name);
            this.registerVariable(variable);
        }

        return variable;
    }

    public MolangExpression parseExpression(@NotNull String expression) throws MolangException {
        return this.parseExpression(expression, null);
    }

    public MolangExpression parseExpression(@NotNull String expression, @Nullable AbstractAnimationData<?> animationData) throws MolangException {
        List<String> lines = new ArrayList<>();

        for (String split : expression.toLowerCase().trim().split(";")) {
            if (!split.trim().isEmpty()) lines.add(split);
        }

        if (lines.isEmpty()) {
            throw new MolangException("Molang expression cannot be blank!");
        }
        else {
            MolangMultiStatement result = new MolangMultiStatement(this);
            this.currentStatement = result;

            try {
                for (String line : lines) {
                    result.expressions.add(this.parseOneLine(line, animationData));
                }
            }
            catch (Exception e) {
                this.currentStatement = null;
                throw e;
            }

            this.currentStatement = null;
            return result;
        }
    }

    protected MolangExpression parseOneLine(String expression, @Nullable AbstractAnimationData<?> animationData) throws MolangException {
        expression = expression.trim();
        if (expression.startsWith("return ")) {
            try {
                return new MolangValue(this, this.parse(expression.substring("return ".length()))).addReturn();
            }
            catch (Exception var5) {
                throw new MolangException("Couldn't parse return '" + expression + "' expression!");
            }
        }
        else {
            List<Object> symbols;
            try {
                symbols = this.breakdownChars(this.breakdown(expression));
            }
            catch (Exception e) {
                throw new MolangException("Couldn't parse '" + expression + "' expression!");
            }

            if (symbols.size() >= 3
                    && symbols.get(0) instanceof String
                    && this.isValueReturner(symbols.get(0))
                    && symbols.get(1).equals("=")
            ) {
                //-----variable stuff-----
                String name = (String) symbols.getFirst();

                //block assignment to functions
                if (this.isFunction(name)) {
                    throw new MolangException("Cannot assign value to function '" + name + "' in '" + expression + "'!");
                }

                //continue with variable stuff
                Variable variable = this.getVariable(name);

                //create a statement-local variable if it doesn't exist anywhere yet
                if (this.currentStatement != null
                        && !this.variables.containsKey(name)
                        && !this.currentStatement.locals.containsKey(name)) {

                    variable = new Variable(this, name);
                    this.currentStatement.locals.put(name, variable);
                }

                //-----other symbols-----
                symbols = symbols.subList(2, symbols.size());

                return new MolangAssignment(this, variable, this.parseSymbolsMolang(symbols, animationData));
            }
            else return new MolangValue(this, this.parseSymbolsMolang(symbols, animationData));
        }
    }

    private IValue parseSymbolsMolang(List<Object> symbols, @Nullable AbstractAnimationData<?> animationData) throws MolangException {
        try {
            return this.parseSymbols(symbols, animationData);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new MolangException("Couldn't parse an expression!");
        }
    }

    public boolean isOperator(String s) {
        return super.isOperator(s) || s.equals("=");
    }
}
