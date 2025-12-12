package anightdazingzoroark.riftlib.molang;

import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.molang.math.MathBuilder;
import anightdazingzoroark.riftlib.molang.math.Variable;
import anightdazingzoroark.riftlib.molang.expressions.MolangAssignment;
import anightdazingzoroark.riftlib.molang.expressions.MolangExpression;
import anightdazingzoroark.riftlib.molang.expressions.MolangMultiStatement;
import anightdazingzoroark.riftlib.molang.expressions.MolangValue;
import anightdazingzoroark.riftlib.molang.functions.CosDegrees;
import anightdazingzoroark.riftlib.molang.functions.SinDegrees;

import java.util.ArrayList;
import java.util.List;

public class MolangParser extends MathBuilder {
    public static final MolangExpression ZERO = new MolangValue((MolangParser)null, new Constant((double)0.0F));
    public static final MolangExpression ONE = new MolangValue((MolangParser)null, new Constant((double)1.0F));
    public static final String RETURN = "return ";
    private MolangMultiStatement currentStatement;

    public MolangParser() {
        this.functions.put("cos", CosDegrees.class);
        this.functions.put("sin", SinDegrees.class);
        this.remap("abs", "math.abs");
        this.remap("acos", "math.acos");
        this.remap("asin", "math.asin");
        this.remap("atan", "math.atan");
        this.remap("atan2", "math.atan2");
        this.remap("ceil", "math.ceil");
        this.remap("clamp", "math.clamp");
        this.remap("cos", "math.cos");
        this.remap("die_roll", "math.die_roll");
        this.remap("die_roll_integer", "math.die_roll_integer");
        this.remap("exp", "math.exp");
        this.remap("floor", "math.floor");
        this.remap("hermite_blend", "math.hermite_blend");
        this.remap("lerp", "math.lerp");
        this.remap("lerprotate", "math.lerprotate");
        this.remap("ln", "math.ln");
        this.remap("max", "math.max");
        this.remap("min", "math.min");
        this.remap("mod", "math.mod");
        this.remap("pi", "math.pi");
        this.remap("pow", "math.pow");
        this.remap("random", "math.random");
        this.remap("random_integer", "math.random_integer");
        this.remap("round", "math.round");
        this.remap("sin", "math.sin");
        this.remap("sqrt", "math.sqrt");
        this.remap("trunc", "math.trunc");
    }

    public void remap(String old, String newName) {
        this.functions.put(newName, this.functions.remove(old));
    }

    public void setValue(String name, double value) {
        Variable variable = this.getVariable(name);
        if (variable != null) {
            variable.set(value);
        }
    }

    protected Variable getVariable(String name) {
        Variable variable = this.currentStatement == null ? null : this.currentStatement.locals.get(name);
        if (variable == null) variable = super.getVariable(name);

        if (variable == null) {
            variable = new Variable(name, (double)0.0F);
            this.register(variable);
        }

        return variable;
    }

    public MolangExpression parseExpression(String expression) throws MolangException {
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
                for(String line : lines) {
                    result.expressions.add(this.parseOneLine(line));
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

    protected MolangExpression parseOneLine(String expression) throws MolangException {
        expression = expression.trim();
        if (expression.startsWith("return ")) {
            try {
                return (new MolangValue(this, this.parse(expression.substring("return ".length())))).addReturn();
            }
            catch (Exception var5) {
                throw new MolangException("Couldn't parse return '" + expression + "' expression!");
            }
        }
        else {
            try {
                List<Object> symbols = this.breakdownChars(this.breakdown(expression));
                if (symbols.size() >= 3 && symbols.get(0) instanceof String && this.isVariable(symbols.get(0)) && symbols.get(1).equals("=")) {
                    String name = (String) symbols.get(0);
                    symbols = symbols.subList(2, symbols.size());
                    Variable variable = this.getVariable(name);
                    if (variable == null) {
                        variable = new Variable(name, 0f);
                        this.register(variable);
                    }

                    return new MolangAssignment(this, variable, this.parseSymbolsMolang(symbols));
                }
                else return new MolangValue(this, this.parseSymbolsMolang(symbols));
            }
            catch (Exception var6) {
                throw new MolangException("Couldn't parse '" + expression + "' expression!");
            }
        }
    }

    private IValue parseSymbolsMolang(List<Object> symbols) throws MolangException {
        try {
            return this.parseSymbols(symbols);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new MolangException("Couldn't parse an expression!");
        }
    }

    protected boolean isOperator(String s) {
        return super.isOperator(s) || s.equals("=");
    }
}
