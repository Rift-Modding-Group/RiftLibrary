package anightdazingzoroark.riftlib.molang.math;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

public class MathBuilder {
    @NotNull
    private final Map<String, Variable> variables = new HashMap<>();
    @NotNull
    private final Map<String, MolangFunction> functions = new HashMap<>();

    /**
     * In this constructor, all math related functions will be registered here
     * */
    public MathBuilder() {
        this.registerFunction("math.pi", 0, (arguments, animationData) -> Math.PI);
        this.registerFunction("math.e", 0, (arguments, animationData) -> Math.E);
        this.registerFunction("math.floor", 1, (arguments, animationData) -> Math.floor(arguments[0].get()));
        this.registerFunction("math.round", 1, (arguments, animationData) -> (double) Math.round(arguments[0].get()));
        this.registerFunction("math.ceil", 1, (arguments, animationData) -> Math.ceil(arguments[0].get()));
        this.registerFunction("math.trunc", 1, (arguments, animationData) -> {
            double value = arguments[0].get();
            return value < 0D ? Math.ceil(value) : Math.floor(value);
        });
        this.registerFunction("math.clamp", 3, (arguments, animationData) -> Math.clamp(arguments[0].get(), arguments[1].get(), arguments[2].get()));
        this.registerFunction("math.max", 2, (arguments, animationData) -> Math.max(arguments[0].get(), arguments[1].get()));
        this.registerFunction("math.min", 2, (arguments, animationData) -> Math.min(arguments[0].get(), arguments[1].get()));
        this.registerFunction("math.abs", 1, (arguments, animationData) -> Math.abs(arguments[0].get()));
        this.registerFunction("math.acos", 1, (arguments, animationData) -> Math.toDegrees(Math.acos(arguments[0].get())));
        this.registerFunction("math.asin", 1, (arguments, animationData) -> Math.toDegrees(Math.asin(arguments[0].get())));
        this.registerFunction("math.atan", 1, (arguments, animationData) -> Math.toDegrees(Math.atan(arguments[0].get())));
        this.registerFunction("math.atan2", 2, (arguments, animationData) -> Math.toDegrees(Math.atan2(arguments[0].get(), arguments[1].get())));
        this.registerFunction("math.cos", 1, (arguments, animationData) -> Math.cos(Math.toRadians(arguments[0].get())));
        this.registerFunction("math.sin", 1, (arguments, animationData) -> Math.sin(Math.toRadians(arguments[0].get())));
        this.registerFunction("math.tan", 1, (arguments, animationData) -> Math.tan(Math.toRadians(arguments[0].get())));
        this.registerFunction("math.exp", 1, (arguments, animationData) -> Math.exp(arguments[0].get()));
        this.registerFunction("math.ln", 1, (arguments, animationData) -> Math.log(arguments[0].get()));
        this.registerFunction("math.sqrt", 1, (arguments, animationData) -> Math.sqrt(arguments[0].get()));
        this.registerFunction("math.mod", 2, (arguments, animationData) -> arguments[0].get() % arguments[1].get());
        this.registerFunction("math.pow", 2, (arguments, animationData) -> Math.pow(arguments[0].get(), arguments[1].get()));
        this.registerFunction("math.lerp", 3, (arguments, animationData) -> Interpolations.lerp(arguments[0].get(), arguments[1].get(), arguments[2].get()));
        this.registerFunction("math.lerprotate", 3, (arguments, animationData) -> Interpolations.lerpYaw(arguments[0].get(), arguments[1].get(), arguments[2].get()));
        this.registerFunction("math.hermite_blend", 1, (arguments, animationData) -> {
            double value = arguments[0].get();
            return 3D * value * value - 2D * value * value * value;
        });
        this.registerFunction("math.die_roll", 3, (arguments, animationData) -> {
            int amount = Math.max(0, (int) Math.floor(arguments[0].get()));
            double lowerBound = Math.min(arguments[1].get(), arguments[2].get());
            double upperBound = Math.max(arguments[1].get(), arguments[2].get());
            if (lowerBound < -Double.MAX_VALUE || upperBound > Double.MAX_VALUE) return 0D;
            double total = 0D;

            for (int i = 0; i < amount; i++) {
                if (lowerBound == upperBound) total += lowerBound;
                else {
                    double exclusiveUpperBound = upperBound == Double.MAX_VALUE ? upperBound : Math.nextUp(upperBound);
                    total += ThreadLocalRandom.current().nextDouble(lowerBound, exclusiveUpperBound);
                }
            }
            return total;
        });
        this.registerFunction("math.die_roll_integer", 3, (arguments, animationData) -> {
            int amount = Math.max(0, (int) Math.floor(arguments[0].get()));
            int lowerBound = (int) Math.ceil(Math.min(arguments[1].get(), arguments[2].get()));
            int upperBound = (int) Math.floor(Math.max(arguments[1].get(), arguments[2].get()));
            if (lowerBound > upperBound) return 0D;

            double total = 0D;
            for (int i = 0; i < amount; i++) {
                total += ThreadLocalRandom.current().nextLong(lowerBound, (long) upperBound + 1L);
            }
            return total;
        });
        this.registerFunction("math.random", 2, (arguments, animationData) -> {
            double lowerBound = arguments[0].get();
            double upperBound = arguments[1].get();
            if (lowerBound > upperBound || lowerBound < -Double.MAX_VALUE || upperBound > Double.MAX_VALUE) return 0D;
            if (lowerBound == upperBound) return lowerBound;
            double exclusiveUpperBound = upperBound == Double.MAX_VALUE ? upperBound : Math.nextUp(upperBound);
            return ThreadLocalRandom.current().nextDouble(lowerBound, exclusiveUpperBound);
        });
        this.registerFunction("math.random_integer", 2, (arguments, animationData) -> {
            int lowerBound = (int) arguments[0].get();
            int upperBound = (int) arguments[1].get();
            if (lowerBound > upperBound) return 0D;
            return (double) ThreadLocalRandom.current().nextLong(lowerBound, (long) upperBound + 1L);
        });
    }

    protected void registerVariable(@NotNull Variable variable) {
        this.variables.put(variable.getName(), variable);
    }

    protected void registerFunction(
            @NotNull String name, int requiredArgumentCount,
            @NotNull BiFunction<IValue[], AbstractAnimationData<?, ?>, ?> operation
    ) {
        this.functions.put(name, new MolangFunction(name, requiredArgumentCount, operation));
    }

    @NotNull
    public IValue parse(@NotNull String expression) throws MolangException {
        return this.parseTokens(this.tokenize(this.lowercaseOutsideStrings(expression)), null);
    }

    @NotNull
    protected List<Token> tokenize(@NotNull String expression) throws MolangException {
        int[] cursor = new int[]{0};
        List<Token> tokens = this.readTokens(expression, cursor, false);
        if (cursor[0] != expression.length()) {
            throw new MolangException("Unexpected closing parenthesis in '" + expression + "'!");
        }
        return tokens;
    }

    @NotNull
    private List<Token> readTokens(@NotNull String expression, int @NotNull [] cursor, boolean nested) throws MolangException {
        List<Token> tokens = new ArrayList<>();

        while (cursor[0] < expression.length()) {
            char character = expression.charAt(cursor[0]);
            if (Character.isWhitespace(character)) {
                cursor[0]++;
                continue;
            }
            if (character == ')') {
                if (!nested) return tokens;
                cursor[0]++;
                return tokens;
            }
            if (character == '(') {
                cursor[0]++;
                tokens.add(new Token(this.readTokens(expression, cursor, true)));
                continue;
            }
            if (character == '\'' || character == '"') {
                char quote = character;
                StringBuilder value = new StringBuilder();
                boolean closed = false;
                cursor[0]++;

                while (cursor[0] < expression.length()) {
                    character = expression.charAt(cursor[0]++);
                    if (character == quote) {
                        closed = true;
                        break;
                    }
                    if (character == '\\' && cursor[0] < expression.length()) {
                        char escaped = expression.charAt(cursor[0]++);
                        value.append(switch (escaped) {
                            case 'n' -> '\n';
                            case 'r' -> '\r';
                            case 't' -> '\t';
                            default -> escaped;
                        });
                    }
                    else value.append(character);
                }

                if (!closed) throw new MolangException("Unterminated string literal in '" + expression + "'!");
                tokens.add(new Token(TokenType.STRING, value.toString()));
                continue;
            }
            if (character == ',') {
                tokens.add(new Token(TokenType.COMMA, ","));
                cursor[0]++;
                continue;
            }

            String pair = cursor[0] + 1 < expression.length() ? expression.substring(cursor[0], cursor[0] + 2) : "";
            if (Operation.fromSign(pair) != null) {
                tokens.add(new Token(TokenType.OPERATOR, pair));
                cursor[0] += 2;
                continue;
            }
            if (character == '=') {
                tokens.add(new Token(TokenType.ASSIGNMENT, "="));
                cursor[0]++;
                continue;
            }
            if (character == '?' || character == ':') {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(character)));
                cursor[0]++;
                continue;
            }
            if (character == '!') {
                tokens.add(new Token(TokenType.PREFIX, "!"));
                cursor[0]++;
                continue;
            }

            Operation operation = Operation.fromSign(String.valueOf(character));
            if (operation != null) {
                boolean expectsValue = tokens.isEmpty()
                        || tokens.getLast().type == TokenType.OPERATOR
                        || tokens.getLast().type == TokenType.PREFIX
                        || tokens.getLast().type == TokenType.COMMA
                        || tokens.getLast().type == TokenType.ASSIGNMENT;
                TokenType type = character == '-' && expectsValue ? TokenType.PREFIX : TokenType.OPERATOR;
                tokens.add(new Token(type, String.valueOf(character)));
                cursor[0]++;
                continue;
            }

            int start = cursor[0];
            while (cursor[0] < expression.length()) {
                character = expression.charAt(cursor[0]);
                if (Character.isWhitespace(character) || "()+-/*%^&|<>=!?:,'\"".indexOf(character) >= 0) break;
                if (!Character.isLetterOrDigit(character) && character != '_' && character != '.') {
                    throw new MolangException("Illegal character '" + character + "' in '" + expression + "'!");
                }
                cursor[0]++;
            }

            if (start == cursor[0]) {
                throw new MolangException("Unexpected character '" + character + "' in '" + expression + "'!");
            }

            String text = expression.substring(start, cursor[0]);
            TokenType type = text.matches("(?:\\d+(?:\\.\\d*)?|\\.\\d+)") ? TokenType.NUMBER : TokenType.IDENTIFIER;
            tokens.add(new Token(type, text));
        }

        if (nested) throw new MolangException("Unclosed parenthesis in '" + expression + "'!");
        return tokens;
    }

    @NotNull
    protected IValue parseTokens(@NotNull List<Token> tokens, @Nullable AbstractAnimationData<?, ?> animationData) throws MolangException {
        if (tokens.isEmpty()) throw new MolangException("Molang expression cannot be blank!");

        int coalescingIndex = -1;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.OPERATOR && token.text.equals(Operation.NULL_COALESCING.getSign())) {
                coalescingIndex = i;
                break;
            }
        }
        if (coalescingIndex >= 0) {
            if (coalescingIndex == 0 || coalescingIndex == tokens.size() - 1) {
                throw new MolangException("Operator '??' is missing an operand!");
            }
            IValue left = this.parseTokens(tokens.subList(0, coalescingIndex), animationData);
            IValue right = this.parseTokens(tokens.subList(coalescingIndex + 1, tokens.size()), animationData);
            return this.createOperator(Operation.NULL_COALESCING, left, right);
        }

        int questionIndex = -1;
        int colonIndex = -1;
        int ternaryDepth = 0;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type != TokenType.OPERATOR) continue;
            if (token.text.equals("?")) {
                if (questionIndex < 0) questionIndex = i;
                ternaryDepth++;
            }
            else if (token.text.equals(":") && questionIndex >= 0) {
                ternaryDepth--;
                if (ternaryDepth == 0) {
                    colonIndex = i;
                    break;
                }
            }
        }

        if (questionIndex >= 0) {
            if (questionIndex == 0 || questionIndex == tokens.size() - 1) {
                throw new MolangException("Incomplete conditional expression!");
            }
            IValue condition = this.parseTokens(tokens.subList(0, questionIndex), animationData);
            IValue ifTrue = this.parseTokens(tokens.subList(questionIndex + 1, colonIndex < 0 ? tokens.size() : colonIndex), animationData);
            IValue ifFalse = colonIndex < 0 ? new Constant(0D) : this.parseTokens(tokens.subList(colonIndex + 1, tokens.size()), animationData);
            return new Ternary(condition, ifTrue, ifFalse);
        }

        Operation selectedOperation = null;
        int selectedIndex = -1;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type != TokenType.OPERATOR) continue;
            Operation operation = Operation.fromSign(token.text);
            if (operation == null) throw new MolangException("Unexpected operator '" + token.text + "'!");

            if (selectedOperation == null || operation.getPrecedence() < selectedOperation.getPrecedence()
                    || operation.getPrecedence() == selectedOperation.getPrecedence() && !operation.isRightAssociative()) {
                selectedOperation = operation;
                selectedIndex = i;
            }
        }

        if (selectedOperation != null) {
            if (selectedIndex == 0 || selectedIndex == tokens.size() - 1) {
                throw new MolangException("Operator '" + selectedOperation.getSign() + "' is missing an operand!");
            }
            IValue left = this.parseTokens(tokens.subList(0, selectedIndex), animationData);
            IValue right = this.parseTokens(tokens.subList(selectedIndex + 1, tokens.size()), animationData);
            return this.createOperator(selectedOperation, left, right);
        }

        Token first = tokens.getFirst();
        if (first.type == TokenType.PREFIX) {
            if (tokens.size() == 1) throw new MolangException("Prefix operator '" + first.text + "' is missing an operand!");
            IValue value = this.parseTokens(tokens.subList(1, tokens.size()), animationData);
            return first.text.equals("!") ? new Negate(value) : new Negative(value);
        }

        if (tokens.size() == 2 && first.type == TokenType.IDENTIFIER && tokens.get(1).type == TokenType.GROUP) {
            List<Token> argumentTokens = tokens.get(1).children;
            List<IValue> arguments = new ArrayList<>();
            int argumentStart = 0;
            for (int i = 0; i <= argumentTokens.size(); i++) {
                if (i < argumentTokens.size() && argumentTokens.get(i).type != TokenType.COMMA) continue;
                if (i == argumentStart) {
                    if (!argumentTokens.isEmpty()) throw new MolangException("Function '" + first.text + "' has an empty argument!");
                }
                else arguments.add(this.parseTokens(argumentTokens.subList(argumentStart, i), animationData));
                argumentStart = i + 1;
            }

            IValue[] argumentArray = arguments.toArray(IValue[]::new);
            if (first.text.startsWith("query.")) return this.createQuery(first.text, argumentArray, animationData);
            return this.createFunctionValue(first.text, argumentArray, animationData);
        }

        if (tokens.size() != 1) throw new MolangException("Could not resolve Molang expression near '" + first.text + "'!");

        return switch (first.type) {
            case NUMBER -> new Constant(Double.parseDouble(first.text));
            case STRING -> new StringValue(first.text);
            case GROUP -> new Group(this.parseTokens(first.children, animationData));
            case IDENTIFIER -> {
                if (first.text.startsWith("query.")) yield this.createQuery(first.text, new IValue[0], animationData);
                MolangFunction function = this.functions.get(first.text);
                if (function != null) yield this.createFunctionValue(first.text, new IValue[0], animationData);
                Variable variable = this.getVariable(first.text);
                if (variable == null) throw new MolangException("Variable '" + first.text + "' couldn't be found!");
                yield variable;
            }
            default -> throw new MolangException("Unexpected token '" + first.text + "'!");
        };
    }

    @NotNull
    private IValue createFunctionValue(
            @NotNull String name, @NotNull IValue[] arguments,
            @Nullable AbstractAnimationData<?, ?> animationData
    ) throws MolangException {
        MolangFunction function = this.functions.get(name);
        if (function == null) throw new MolangException("Function '" + name + "' couldn't be found!");
        if (arguments.length < function.getRequiredArgumentCount()) {
            throw new MolangException("Function '" + name + "' requires at least " + function.getRequiredArgumentCount()
                    + " arguments, but " + arguments.length + " were given!");
        }
        return new MolangFunctionValue(function, arguments, animationData);
    }

    @Nullable
    protected Variable getVariable(@NotNull String name) {
        return this.variables.get(name);
    }

    @NotNull
    protected IValue createOperator(@NotNull Operation operation, @NotNull IValue left, @NotNull IValue right) throws MolangException {
        if (operation == Operation.ARROW) throw new MolangException("The arrow operator requires a Molang parser context!");
        return new Operator(operation, left, right);
    }

    @NotNull
    protected IValue createQuery(
            @NotNull String name, @NotNull IValue[] arguments,
            @Nullable AbstractAnimationData<?, ?> animationData
    ) throws MolangException {
        throw new MolangException("Queries require a Molang parser context!");
    }

    public boolean isFunction(@NotNull String name) {
        return name.startsWith("query.") || name.startsWith("math.") || name.startsWith("function.");
    }

    @NotNull
    protected String lowercaseOutsideStrings(@NotNull String expression) {
        StringBuilder result = new StringBuilder(expression.length());
        boolean inString = false;
        boolean escaping = false;
        char quote = 0;

        for (int i = 0; i < expression.length(); i++) {
            char character = expression.charAt(i);
            if (inString) {
                result.append(character);
                if (escaping) escaping = false;
                else if (character == '\\') escaping = true;
                else if (character == quote) inString = false;
            }
            else if (character == '\'' || character == '"') {
                inString = true;
                quote = character;
                result.append(character);
            }
            else result.append(Character.toLowerCase(character));
        }
        return result.toString();
    }

    protected enum TokenType {
        NUMBER,
        IDENTIFIER,
        STRING,
        GROUP,
        OPERATOR,
        PREFIX,
        COMMA,
        ASSIGNMENT
    }

    protected static class Token {
        @NotNull
        private final TokenType type;
        @NotNull
        private final String text;
        @NotNull
        private final List<Token> children;

        protected Token(@NotNull TokenType type, @NotNull String text) {
            this.type = type;
            this.text = text;
            this.children = List.of();
        }

        protected Token(@NotNull List<Token> children) {
            this.type = TokenType.GROUP;
            this.text = "()";
            this.children = List.copyOf(children);
        }

        @NotNull
        public TokenType getType() {
            return this.type;
        }

        @NotNull
        public String getText() {
            return this.text;
        }
    }
}
