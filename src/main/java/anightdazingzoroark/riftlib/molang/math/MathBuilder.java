package anightdazingzoroark.riftlib.molang.math;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;

public class MathBuilder {
    public final Map<String, Variable> variables = new HashMap<>();
    public final Map<String, MolangFunction> functions = new HashMap<>();

    /**
     * In this constructor, all math related functions will be registered here
     * */
    public MathBuilder() {
        this.registerFunction("math.pi", 0, (values, animData) -> Math.PI);
        this.registerFunction("math.e", 0, (values, animData) -> Math.E);
        this.registerFunction("math.floor", 1, (values, animData) -> Math.floor(values[0].get()));
        this.registerFunction("math.round", 1, (values, animData) -> (double) Math.round(values[0].get()));
        this.registerFunction("math.ceil", 1, (values, animData) -> Math.ceil(values[0].get()));
        this.registerFunction("math.trunc", 1, (values, animData) -> {
            double value = values[0].get();
            return value < 0D ? Math.ceil(value) : Math.floor(value);
        });
        this.registerFunction("math.clamp", 3, (values, animData) -> Math.clamp(values[0].get(), values[1].get(), values[2].get()));
        this.registerFunction("math.max", 2, (values, animData) -> Math.max(values[0].get(), values[1].get()));
        this.registerFunction("math.min", 2, (values, animData) -> Math.min(values[0].get(), values[1].get()));
        this.registerFunction("math.abs", 1, (values, animData) -> Math.abs(values[0].get()));
        this.registerFunction("math.acos", 1, (values, animData) -> Math.toDegrees(Math.acos(values[0].get())));
        this.registerFunction("math.asin", 1, (values, animData) -> Math.toDegrees(Math.asin(values[0].get())));
        this.registerFunction("math.atan", 1, (values, animData) -> Math.toDegrees(Math.atan(values[0].get())));
        this.registerFunction("math.atan2", 2, (values, animData) -> Math.toDegrees(Math.atan2(values[0].get(), values[1].get())));
        this.registerFunction("math.cos", 1, (values, animData) -> Math.cos(Math.toRadians(values[0].get())));
        this.registerFunction("math.sin", 1, (values, animData) -> Math.sin(Math.toRadians(values[0].get())));
        this.registerFunction("math.tan", 1, (values, animData) -> Math.tan(Math.toRadians(values[0].get())));
        this.registerFunction("math.exp", 1, (values, animData) -> Math.exp(values[0].get()));
        this.registerFunction("math.ln", 1, (values, animData) -> Math.log(values[0].get()));
        this.registerFunction("math.sqrt", 1, (values, animData) -> Math.sqrt(values[0].get()));
        this.registerFunction("math.mod", 2, (values, animData) -> values[0].get() % values[1].get());
        this.registerFunction("math.pow", 2, (values, animData) -> Math.pow(values[0].get(), values[1].get()));
        this.registerFunction("math.lerp", 3, (values, animData) -> {
            return Interpolations.lerp(values[0].get(), values[1].get(), values[2].get());
        });
        this.registerFunction("math.lerprotate", 3, (values, animData) -> {
            return Interpolations.lerpYaw(values[0].get(), values[1].get(), values[2].get());
        });
        this.registerFunction("math.hermite_blend", 1, (values, animData) -> {
            double min = Math.ceil(values[0].get());
            return Math.floor(3D * Math.pow(min, 2D) - 2D * Math.pow(min, 3D));
        });
        this.registerFunction("math.die_roll", 3, (values, animData) -> {
            int amount = Math.max(0, (int) Math.floor(values[0].get()));
            double lowerBound = Math.min(values[1].get(), values[2].get());
            double upperBound = Math.max(values[1].get(), values[2].get());

            double total = 0D;
            for (int i = 0; i < amount; i++) {
                total += Math.random() * (upperBound - lowerBound) + lowerBound;
            }

            return total;
        });
        this.registerFunction("math.die_roll_integer", 3, (values, animData) -> {
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
        this.registerFunction("math.random", 2, (values, animData) -> {
            double lowerBound = values[0].get();
            double upperBound = values[1].get();
            if (lowerBound > upperBound) return 0D;

            Random random = new Random();
            return random.nextDouble(lowerBound, upperBound);
        });
        this.registerFunction("math.random_integer", 2, (values, animData) -> {
            int lowerBound = (int) values[0].get();
            int upperBound = (int) values[1].get();
            if (lowerBound > upperBound) return 0D;

            Random random = new Random();
            return (double) random.nextInt(lowerBound, upperBound);
        });
    }

    //-----registry stuff starts here-----
    protected void registerVariable(Variable variable) {
        this.variables.put(variable.getName(), variable);
    }

    protected void registerFunction(String name, int argCount, BiFunction<IValue[], AbstractAnimationData<?, ?>, Double> operation) {
        this.functions.put(name, new MolangFunction(name) {
            @Override
            public int requiredArgCount() {
                return argCount;
            }

            @Override
            @NotNull
            public BiFunction<IValue[], AbstractAnimationData<?, ?>, Double> operation() {
                return operation;
            }
        });
    }
    //-----registry stuff ends here-----

    public IValue parse(String expression) throws Exception {
        return this.parseSymbols(this.breakdownChars(this.breakdown(expression)));
    }

    public String[] breakdown(String expression) throws Exception {
        StringBuilder normalized = new StringBuilder();
        int left = 0;
        int right = 0;
        boolean inString = false;
        boolean escaping = false;
        char quote = 0;

        for (int i = 0; i < expression.length(); ++i) {
            char c = expression.charAt(i);

            if (inString) {
                normalized.append(c);

                if (escaping) escaping = false;
                else if (c == '\\') escaping = true;
                else if (c == quote) {
                    inString = false;
                    quote = 0;
                }

                continue;
            }

            if (this.isQuote(c)) {
                inString = true;
                quote = c;
                normalized.append(c);
                continue;
            }

            if (Character.isWhitespace(c)) continue;

            if (!this.isLegalExpressionCharacter(c)) {
                throw new Exception("Given expression '" + expression + "' contains illegal characters!");
            }

            if (c == '(') ++left;
            else if (c == ')') ++right;

            normalized.append(c);
        }

        if (inString) {
            throw new Exception("Given expression '" + expression + "' has an unterminated string literal!");
        }

        if (left != right) {
            throw new Exception("Given expression '" + expression + "' has more uneven amount of parenthesis, there are " + left + " open and " + right + " closed!");
        }

        return normalized.toString().split("(?!^)");
    }

    public List<Object> breakdownChars(String[] chars) {
        List<Object> symbols = new ArrayList<>();
        String buffer = "";
        int len = chars.length;

        for (int i = 0; i < len; ++i) {
            String s = chars[i];
            if (this.isQuote(s)) {
                if (!buffer.isEmpty()) {
                    symbols.add(buffer);
                    buffer = "";
                }

                StringBuilder literal = new StringBuilder();
                boolean escaping = false;

                for (int j = i + 1; j < len; ++j) {
                    String c = chars[j];

                    if (escaping) {
                        literal.append(this.unescapeStringCharacter(c.charAt(0)));
                        escaping = false;
                        continue;
                    }

                    if (c.equals("\\")) {
                        escaping = true;
                        continue;
                    }

                    if (c.equals(s)) {
                        symbols.add(new StringValue(literal.toString()));
                        i = j;
                        break;
                    }

                    literal.append(c);
                }

                continue;
            }

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
                        if (this.isQuote(c)) {
                            String stringQuote = c;
                            buffer = buffer + c;
                            boolean escaping = false;

                            for (++j; j < len; ++j) {
                                c = chars[j];
                                buffer = buffer + c;

                                if (escaping) {
                                    escaping = false;
                                }
                                else if (c.equals("\\")) {
                                    escaping = true;
                                }
                                else if (c.equals(stringQuote)) {
                                    break;
                                }
                            }

                            continue;
                        }

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

    public IValue parseSymbols(List<Object> symbols) throws MolangException {
        return this.parseSymbols(symbols, null);
    }

    public IValue parseSymbols(List<Object> symbols, @Nullable AbstractAnimationData<?, ?> animationData) throws MolangException {
        IValue ternary = this.tryTernary(symbols);
        if (ternary != null) return ternary;
        else {
            int size = symbols.size();
            if (size == 1) return this.valueFromObject(symbols.getFirst(), animationData);
            else {
                if (size == 2) {
                    Object first = symbols.get(0);
                    Object second = symbols.get(1);
                    if ((this.isValueReturner(first) || first.equals("-")) && second instanceof List) {
                        //if this is a function with no args we're dealing with, return an exception
                        String funcName = (String) first;
                        if (this.isFunctionNoArgs(funcName, animationData)) {
                            throw new MolangException("Function "+funcName+" does not accept any arguments, yet it is treated as if it does!");
                        }
                        //normal function creation
                        return this.createFunction(funcName, (List) second, animationData);
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
                            IValue leftValue = this.parseSymbols(symbols.subList(0, leftOp), animationData);
                            IValue rightValue = this.parseSymbols(symbols.subList(leftOp + 1, size), animationData);
                            return new Operator(left, leftValue, rightValue);
                        }

                        if (left.value > right.value) {
                            Operation initial = this.operationForOperator((String)symbols.get(lastOp));
                            if (initial.value < left.value) {
                                IValue leftValue = this.parseSymbols(symbols.subList(0, lastOp), animationData);
                                IValue rightValue = this.parseSymbols(symbols.subList(lastOp + 1, size), animationData);
                                return new Operator(initial, leftValue, rightValue);
                            }

                            IValue leftValue = this.parseSymbols(symbols.subList(0, op), animationData);
                            IValue rightValue = this.parseSymbols(symbols.subList(op + 1, size), animationData);
                            return new Operator(right, leftValue, rightValue);
                        }
                    }
                }

                Operation operation = this.operationForOperator((String)symbols.get(lastOp));
                return new Operator(operation, this.parseSymbols(symbols.subList(0, lastOp), animationData), this.parseSymbols(symbols.subList(lastOp + 1, size), animationData));
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

    protected IValue tryTernary(List<Object> symbols) throws MolangException {
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

    protected IValue createFunction(String first, List<Object> args, @Nullable AbstractAnimationData<?, ?> animationData) throws MolangException {
        if (first.equals("!")) {
            return new Negate(this.parseSymbols(args, animationData));
        }
        else if (first.startsWith("!") && first.length() > 1) {
            return new Negate(this.createFunction(first.substring(1), args, animationData));
        }
        else if (first.equals("-")) {
            return new Negative(new Group(this.parseSymbols(args, animationData)));
        }
        else if (first.startsWith("-") && first.length() > 1) {
            return new Negative(this.createFunction(first.substring(1), args, animationData));
        }
        else if (this.functions.containsKey(first)) {
            return this.parseFunctionOrQuery(first, args, animationData, this.functions);
        }
        else if (animationData != null && animationData.getMolangQueries().containsKey(first)) {
            return this.parseFunctionOrQuery(first, args, animationData, animationData.getMolangQueries());
        }

        throw new MolangException("Function '" + first + "' couldn't be found!");
    }

    private IValue parseFunctionOrQuery(
            String first, List<Object> args, @Nullable AbstractAnimationData<?, ?> animationData,
            Map<String, MolangFunction> functionMapRef
    ) throws MolangException {
        List<IValue> values = new ArrayList<>();
        List<Object> buffer = new ArrayList<>();

        for (Object o : args) {
            if (o.equals(",")) {
                values.add(this.parseSymbols(buffer, animationData));
                buffer.clear();
            }
            else buffer.add(o);
        }

        if (!buffer.isEmpty()) {
            values.add(this.parseSymbols(buffer, animationData));
        }

        MolangFunction function = functionMapRef.get(first);
        if (function == null) throw new MolangException("Function '" + first + "' couldn't be found!");
        IValue[] argsArr = values.toArray(new IValue[0]);

        //exception for unexpected argument counts
        //note that a negative arg count for a function means it has no limit
        if (argsArr.length < function.requiredArgCount() && function.requiredArgCount() >= 0) {
            String message = String.format(
                    "Function '%s' requires at least %s arguments. %s are given!",
                    function.name, function.requiredArgCount(), argsArr.length
            );
            throw new MolangException(message);
        }

        return new IValue() {
            @Override
            public double get() {
                return function.operation().apply(argsArr, animationData);
            }
        };
    }

    public IValue valueFromObject(Object object, @Nullable AbstractAnimationData<?, ?> animationData) throws MolangException {
        if (object instanceof String symbol) {
            if (symbol.startsWith("!")) {
                return new Negate(this.valueFromObject(symbol.substring(1), animationData));
            }

            if (this.isDecimal(symbol)) {
                return new Constant(Double.parseDouble(symbol));
            }

            if (this.isValueReturner(symbol)) {
                //negating a value returner
                if (symbol.startsWith("-")) {
                    symbol = symbol.substring(1);
                    return new Negative(this.valueFromObject(symbol, animationData));
                }
                //this is for functions that have no args. functions w no args have no parenthesis at all
                else if (this.isFunctionNoArgs(symbol, animationData)) {
                    return this.createFunction(symbol, List.of(), animationData);
                }
                //this is for good ol variables
                else {
                    IValue value = this.getVariable(symbol);
                    if (value != null) return value;
                }
            }
        }
        else if (object instanceof IValue value) {
            return value;
        }
        else if (object instanceof List) {
            return new Group(this.parseSymbols((List) object, animationData));
        }

        throw new MolangException("Given object couldn't be converted to value! " + object);
    }

    protected String lowercaseOutsideStrings(String expression) {
        StringBuilder builder = new StringBuilder();
        boolean inString = false;
        boolean escaping = false;
        char quote = 0;

        for (int i = 0; i < expression.length(); ++i) {
            char c = expression.charAt(i);

            if (inString) {
                builder.append(c);

                if (escaping) escaping = false;
                else if (c == '\\') escaping = true;
                else if (c == quote) {
                    inString = false;
                    quote = 0;
                }

                continue;
            }

            if (this.isQuote(c)) {
                inString = true;
                quote = c;
                builder.append(c);
            }
            else builder.append(Character.toLowerCase(c));
        }

        return builder.toString();
    }

    protected List<String> splitStatements(String expression) {
        List<String> statements = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        boolean inString = false;
        boolean escaping = false;
        char quote = 0;

        for (int i = 0; i < expression.length(); ++i) {
            char c = expression.charAt(i);

            if (inString) {
                buffer.append(c);

                if (escaping) {
                    escaping = false;
                }
                else if (c == '\\') {
                    escaping = true;
                }
                else if (c == quote) {
                    inString = false;
                    quote = 0;
                }

                continue;
            }

            if (this.isQuote(c)) {
                inString = true;
                quote = c;
                buffer.append(c);
            }
            else if (c == ';') {
                statements.add(buffer.toString());
                buffer.setLength(0);
            }
            else {
                buffer.append(c);
            }
        }

        statements.add(buffer.toString());
        return statements;
    }

    protected Variable getVariable(String name) {
        return this.variables.get(name);
    }

    protected Operation operationForOperator(String op) throws MolangException {
        for (Operation operation : Operation.values()) {
            if (operation.sign.equals(op)) return operation;
        }

        throw new MolangException("There is no such operator '" + op + "'!");
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
        String lower = s.toLowerCase();
        return lower.startsWith("query.") || lower.startsWith("math.") || lower.startsWith("function.");
    }

    protected boolean isDecimal(String s) {
        return s.matches("^-?\\d+(\\.\\d+)?$");
    }

    protected boolean isQuote(String s) {
        return s.length() == 1 && this.isQuote(s.charAt(0));
    }

    protected boolean isQuote(char c) {
        return c == '\'' || c == '"';
    }

    protected boolean isLegalExpressionCharacter(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9')
                || "_+-/*%^&|<>=!?:.,()".indexOf(c) >= 0;
    }

    protected char unescapeStringCharacter(char c) {
        return switch (c) {
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            default -> c;
        };
    }

    protected boolean isFunctionNoArgs(String s, AbstractAnimationData<?, ?> animationData) {
        for (Map.Entry<String, MolangFunction> functionEntry : this.functions.entrySet()) {
            if (functionEntry.getKey().equals(s) && functionEntry.getValue().requiredArgCount() <= 0) return true;
        }
        for (Map.Entry<String, MolangFunction> functionEntry : animationData.getMolangQueries().entrySet()) {
            if (functionEntry.getKey().equals(s) && functionEntry.getValue().requiredArgCount() <= 0) return true;
        }
        return false;
    }
}
