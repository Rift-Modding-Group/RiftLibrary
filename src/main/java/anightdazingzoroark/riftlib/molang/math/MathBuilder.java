package anightdazingzoroark.riftlib.molang.math;

import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MathBuilder {
    public final Map<String, Variable> variables = new HashMap<>();
    public final Map<String, Function> functions = new HashMap<>();

    public MathBuilder() {
        this.registerFunction("math.pi", 0, values -> Math.PI);
        this.registerFunction("math.e", 0, values -> Math.E);
        this.registerFunction("math.floor", 1, values -> Math.floor(values[0].get()));
        this.registerFunction("math.round", 1, values -> (double) Math.round(values[0].get()));
        this.registerFunction("math.ceil", 1, values -> Math.ceil(values[0].get()));
        this.registerFunction("math.trunc", 1, values -> {
            double value = values[0].get();
            return value < 0D ? Math.ceil(value) : Math.floor(value);
        });
        this.registerFunction("math.clamp", 3, values -> Math.clamp(values[0].get(), values[1].get(), values[2].get()));
        this.registerFunction("math.max", 2, values -> Math.max(values[0].get(), values[1].get()));
        this.registerFunction("math.min", 2, values -> Math.min(values[0].get(), values[1].get()));
        this.registerFunction("math.abs", 1, values -> Math.abs(values[0].get()));
        this.registerFunction("math.acos", 1, values -> Math.toDegrees(Math.acos(values[0].get())));
        this.registerFunction("math.asin", 1, values -> Math.toDegrees(Math.asin(values[0].get())));
        this.registerFunction("math.atan", 1, values -> Math.toDegrees(Math.atan(values[0].get())));
        this.registerFunction("math.atan2", 2, values -> Math.toDegrees(Math.atan2(values[0].get(), values[1].get())));
        this.registerFunction("math.cos", 1, values -> Math.cos(Math.toRadians(values[0].get())));
        this.registerFunction("math.sin", 1, values -> Math.sin(Math.toRadians(values[0].get())));
        this.registerFunction("math.tan", 1, values -> Math.tan(Math.toRadians(values[0].get())));
        this.registerFunction("math.exp", 1, values -> Math.exp(values[0].get()));
        this.registerFunction("math.ln", 1, values -> Math.log(values[0].get()));
        this.registerFunction("math.sqrt", 1, values -> Math.sqrt(values[0].get()));
        this.registerFunction("math.mod", 2, values -> values[0].get() % values[1].get());
        this.registerFunction("math.pow", 2, values -> Math.pow(values[0].get(), values[1].get()));
        this.registerFunction("math.lerp", 3, values -> {
            return Interpolations.lerp(values[0].get(), values[1].get(), values[2].get());
        });
        this.registerFunction("math.lerprotate", 3, values -> {
            return Interpolations.lerpYaw(values[0].get(), values[1].get(), values[2].get());
        });
        this.registerFunction("math.hermite_blend", 1, values -> {
            double min = Math.ceil(values[0].get());
            return Math.floor(3D * Math.pow(min, 2D) - 2D * Math.pow(min, 3D));
        });
        this.registerFunction("math.die_roll", 3, values -> {
            int amount = Math.max(0, (int) Math.floor(values[0].get()));
            double lowerBound = Math.min(values[1].get(), values[2].get());
            double upperBound = Math.max(values[1].get(), values[2].get());

            double total = 0D;
            for (int i = 0; i < amount; i++) {
                total += Math.random() * (upperBound - lowerBound) + lowerBound;
            }

            return total;
        });
        this.registerFunction("math.die_roll_integer", 3, values -> {
            int amount = Math.max(0, (int) Math.floor(values[0].get()));
            int lowerBound = (int) Math.ceil(Math.min(values[1].get(), values[2].get()));
            int upperBound = (int) Math.floor(Math.max(values[1].get(), values[2].get()));
            if (lowerBound > upperBound) return 0D;

            double total = 0D;
            for (int i = 0; i < amount; i++) {
                total += Math.floor(Math.random() * (upperBound - lowerBound + 1)) + lowerBound;
            }

            return total;
        });
        this.registerFunction("math.random", 2, values -> {
            int lowerBound = (int) Math.ceil(Math.min(values[0].get(), values[1].get()));
            int upperBound = (int) Math.floor(Math.max(values[0].get(), values[1].get()));
            if (lowerBound > upperBound) return 0D;

            java.util.Random random = new java.util.Random();
            return random.nextDouble(lowerBound, upperBound);
        });
        this.registerFunction("math.random_integer", 2, values -> {
            int lowerBound = (int) Math.ceil(Math.min(values[1].get(), values[2].get()));
            int upperBound = (int) Math.floor(Math.max(values[1].get(), values[2].get()));
            if (lowerBound > upperBound) return 0D;

            java.util.Random random = new java.util.Random();
            return (double) random.nextInt(lowerBound, upperBound);
        });
    }

    protected void registerVariable(Variable variable) {
        this.variables.put(variable.getName(), variable);
    }

    private void registerFunction(String name, int argCount, java.util.function.Function<IValue[], Double> operation) {
        this.functions.put(name, new Function(name) {
            @Override
            public int requiredArgCount() {
                return argCount;
            }

            @Override
            @NotNull
            public java.util.function.Function<IValue[], Double> operation() {
                return operation;
            }
        });
    }

    public IValue parse(String expression) throws Exception {
        return this.parseSymbols(this.breakdownChars(this.breakdown(expression)));
    }

    public String[] breakdown(String expression) throws Exception {
        if (!expression.matches("^[\\w\\d\\s_+-/*%^&|<>=!?:.,()]+$")) {
            throw new Exception("Given expression '" + expression + "' contains illegal characters!");
        }
        else {
            expression = expression.replaceAll("\\s+", "");
            String[] chars = expression.split("(?!^)");
            int left = 0;
            int right = 0;

            for (String s : chars) {
                if (s.equals("(")) ++left;
                else if (s.equals(")")) ++right;
            }

            if (left != right) {
                throw new Exception("Given expression '" + expression + "' has more uneven amount of parenthesis, there are " + left + " open and " + right + " closed!");
            }
            else return chars;
        }
    }

    public List<Object> breakdownChars(String[] chars) {
        List<Object> symbols = new ArrayList<>();
        String buffer = "";
        int len = chars.length;

        for (int i = 0; i < len; ++i) {
            String s = chars[i];
            boolean longOperator = i > 0 && this.isOperator(chars[i - 1] + s);
            if (!this.isOperator(s) && !longOperator && !s.equals(",")) {
                if (s.equals("(")) {
                    if (!buffer.isEmpty()) {
                        symbols.add(buffer);
                        buffer = "";
                    }

                    int counter = 1;

                    for (int j = i + 1; j < len; ++j) {
                        String c = chars[j];
                        if (c.equals("(")) ++counter;
                        else if (c.equals(")")) --counter;

                        if (counter == 0) {
                            symbols.add(this.breakdownChars(buffer.split("(?!^)")));
                            i = j;
                            buffer = "";
                            break;
                        }

                        buffer = buffer + c;
                    }
                }
                else buffer = buffer + s;
            }
            else {
                if (s.equals("-")) {
                    int size = symbols.size();
                    boolean isFirst = size == 0 && buffer.isEmpty();
                    boolean isOperatorBehind = size > 0 && (this.isOperator(symbols.get(size - 1)) || symbols.get(size - 1).equals(",")) && buffer.isEmpty();
                    if (isFirst || isOperatorBehind) {
                        buffer = buffer + s;
                        continue;
                    }
                }

                if (longOperator) {
                    s = chars[i - 1] + s;
                    buffer = buffer.substring(0, buffer.length() - 1);
                }

                if (!buffer.isEmpty()) {
                    symbols.add(buffer);
                    buffer = "";
                }

                symbols.add(s);
            }
        }

        if (!buffer.isEmpty()) symbols.add(buffer);

        return symbols;
    }

    public IValue parseSymbols(List<Object> symbols) throws Exception {
        IValue ternary = this.tryTernary(symbols);
        if (ternary != null) return ternary;
        else {
            int size = symbols.size();
            if (size == 1) return this.valueFromObject(symbols.getFirst());
            else {
                if (size == 2) {
                    Object first = symbols.get(0);
                    Object second = symbols.get(1);
                    if ((this.isValueReturner(first) || first.equals("-")) && second instanceof List) {
                        //if this is a function with no args we're dealing with, return an exception
                        String funcName = (String) first;
                        if (this.isFunctionNoArgs(funcName)) {
                            throw new Exception("Function "+funcName+" does not accept any arguments, yet it is treated as if it does!");
                        }
                        //normal function creation
                        return this.createFunction(funcName, (List) second);
                    }
                }

                int lastOp = this.seekLastOperator(symbols);

                int leftOp;
                for (int op = lastOp; op != -1; op = leftOp) {
                    leftOp = this.seekLastOperator(symbols, op - 1);
                    if (leftOp != -1) {
                        Operation left = this.operationForOperator((String)symbols.get(leftOp));
                        Operation right = this.operationForOperator((String)symbols.get(op));
                        if (right.value > left.value) {
                            IValue leftValue = this.parseSymbols(symbols.subList(0, leftOp));
                            IValue rightValue = this.parseSymbols(symbols.subList(leftOp + 1, size));
                            return new Operator(left, leftValue, rightValue);
                        }

                        if (left.value > right.value) {
                            Operation initial = this.operationForOperator((String)symbols.get(lastOp));
                            if (initial.value < left.value) {
                                IValue leftValue = this.parseSymbols(symbols.subList(0, lastOp));
                                IValue rightValue = this.parseSymbols(symbols.subList(lastOp + 1, size));
                                return new Operator(initial, leftValue, rightValue);
                            }

                            IValue leftValue = this.parseSymbols(symbols.subList(0, op));
                            IValue rightValue = this.parseSymbols(symbols.subList(op + 1, size));
                            return new Operator(right, leftValue, rightValue);
                        }
                    }
                }

                Operation operation = this.operationForOperator((String)symbols.get(lastOp));
                return new Operator(operation, this.parseSymbols(symbols.subList(0, lastOp)), this.parseSymbols(symbols.subList(lastOp + 1, size)));
            }
        }
    }

    protected int seekLastOperator(List<Object> symbols) {
        return this.seekLastOperator(symbols, symbols.size() - 1);
    }

    protected int seekLastOperator(List<Object> symbols, int offset) {
        for(int i = offset; i >= 0; --i) {
            Object o = symbols.get(i);
            if (this.isOperator(o)) {
                return i;
            }
        }

        return -1;
    }

    protected int seekFirstOperator(List<Object> symbols) {
        return this.seekFirstOperator(symbols, 0);
    }

    protected int seekFirstOperator(List<Object> symbols, int offset) {
        int i = offset;

        for(int size = symbols.size(); i < size; ++i) {
            Object o = symbols.get(i);
            if (this.isOperator(o)) {
                return i;
            }
        }

        return -1;
    }

    protected IValue tryTernary(List<Object> symbols) throws Exception {
        int question = -1;
        int questions = 0;
        int colon = -1;
        int colons = 0;
        int size = symbols.size();

        for(int i = 0; i < size; ++i) {
            Object object = symbols.get(i);
            if (object instanceof String) {
                if (object.equals("?")) {
                    if (question == -1) question = i;

                    ++questions;
                }
                else if (object.equals(":")) {
                    if (colons + 1 == questions && colon == -1) {
                        colon = i;
                    }

                    ++colons;
                }
            }
        }

        if (questions == colons && question > 0 && question + 1 < colon && colon < size - 1) {
            return new Ternary(this.parseSymbols(symbols.subList(0, question)), this.parseSymbols(symbols.subList(question + 1, colon)), this.parseSymbols(symbols.subList(colon + 1, size)));
        }
        else return null;
    }

    protected IValue createFunction(String first, List<Object> args) throws Exception {
        if (first.equals("!")) {
            return new Negate(this.parseSymbols(args));
        }
        else if (first.startsWith("!") && first.length() > 1) {
            return new Negate(this.createFunction(first.substring(1), args));
        }
        else if (first.equals("-")) {
            return new Negative(new Group(this.parseSymbols(args)));
        }
        else if (first.startsWith("-") && first.length() > 1) {
            return new Negative(this.createFunction(first.substring(1), args));
        }
        else if (!this.functions.containsKey(first)) {
            throw new Exception("Function '" + first + "' couldn't be found!");
        }
        else {
            List<IValue> values = new ArrayList<>();
            List<Object> buffer = new ArrayList<>();

            for (Object o : args) {
                if (o.equals(",")) {
                    values.add(this.parseSymbols(buffer));
                    buffer.clear();
                }
                else buffer.add(o);
            }

            if (!buffer.isEmpty()) {
                values.add(this.parseSymbols(buffer));
            }

            Function function = this.functions.get(first);
            IValue[] argsArr = values.toArray(new IValue[0]);
            if (argsArr.length < function.requiredArgCount()) {
                String message = String.format(
                        "Function '%s' requires at least %s arguments. %s are given!",
                        function.name, function.requiredArgCount(), argsArr.length
                );
                throw new Exception(message);
            }

            return new IValue() {
                @Override
                public double get() {
                    return function.operation().apply(argsArr);
                }
            };
        }
    }

    public IValue valueFromObject(Object object) throws Exception {
        if (object instanceof String symbol) {
            if (symbol.startsWith("!")) {
                return new Negate(this.valueFromObject(symbol.substring(1)));
            }

            if (this.isDecimal(symbol)) {
                return new Constant(Double.parseDouble(symbol));
            }

            if (this.isValueReturner(symbol)) {
                //negating a value returner
                if (symbol.startsWith("-")) {
                    symbol = symbol.substring(1);
                    return new Negative(this.valueFromObject(symbol));
                }
                //this is for functions that have no args. functions w no args have no parenthesis at all
                else if (this.isFunctionNoArgs(symbol)) {
                    return this.createFunction(symbol, List.of());
                }
                //this is for good ol variables
                else {
                    IValue value = this.getVariable(symbol);
                    if (value != null) return value;
                }
            }
        }
        else if (object instanceof List) {
            return new Group(this.parseSymbols((List) object));
        }

        throw new Exception("Given object couldn't be converted to value! " + object);
    }

    protected Variable getVariable(String name) {
        return this.variables.get(name);
    }

    protected Operation operationForOperator(String op) throws Exception {
        for (Operation operation : Operation.values()) {
            if (operation.sign.equals(op)) return operation;
        }

        throw new Exception("There is no such operator '" + op + "'!");
    }

    //a "value returner" is basically a non-alphanumeric representation of some kind of value
    //this includes variables and functions
    public boolean isValueReturner(Object o) {
        return o instanceof String string && !this.isDecimal(string) && !this.isOperator(string);
    }

    public boolean isOperator(Object o) {
        return o instanceof String string && this.isOperator(string);
    }

    public boolean isOperator(String s) {
        return Operation.OPERATORS.contains(s) || s.equals("?") || s.equals(":");
    }

    //this is to block value assignments to functions, like the ones for
    //math and the ones for molang queries.
    public boolean isFunction(String s) {
        return s.startsWith("query.") || s.startsWith("math.");
    }

    protected boolean isDecimal(String s) {
        return s.matches("^-?\\d+(\\.\\d+)?$");
    }

    protected boolean isFunctionNoArgs(String s) {
        for (Map.Entry<String, Function> functionEntry : this.functions.entrySet()) {
            if (functionEntry.getKey().equals(s) && functionEntry.getValue().requiredArgCount() <= 0) return true;
        }
        return false;
    }
}