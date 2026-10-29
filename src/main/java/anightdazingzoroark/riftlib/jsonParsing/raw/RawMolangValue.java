package anightdazingzoroark.riftlib.jsonParsing.raw;

import anightdazingzoroark.riftlib.core.ConstantValue;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.expressions.MolangExpression;
import anightdazingzoroark.riftlib.molang.expressions.MolangValue;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Made to help out with json fields that are either
 * a molang expression or a numerical value
 *
 */
public class RawMolangValue {
    @Nullable
    public String stringValue;
    @Nullable
    public Double numericalValue;

    /**
     * Compiles this value against the parser belonging to the emitter or particle.
     */
    @NotNull
    public MolangExpression compile(@NotNull MolangParser parser) {
        if (this.stringValue != null) {
            try {
                return parser.parseExpression(this.stringValue);
            }
            catch (MolangException exception) {
                throw new IllegalArgumentException("Invalid Molang expression: " + this.stringValue, exception);
            }
        }
        if (this.numericalValue != null) return new MolangValue(new Constant(this.numericalValue));
        throw new IllegalArgumentException("Expected a number or Molang expression");
    }

    @NotNull
    public static IValue[] compileArray(@NotNull MolangParser parser, @NotNull RawMolangValue[] values) {
        IValue[] result = new IValue[values.length];
        for (int i = 0; i < values.length; i++) result[i] = values[i].compile(parser);
        return result;
    }

    @NotNull
    public static RawMolangValue fromValue(@NotNull IValue value) {
        RawMolangValue result = new RawMolangValue();
        if (value instanceof MolangExpression expression) {
            JsonElement json = expression.toJson();
            if (json.getAsJsonPrimitive().isNumber()) result.numericalValue = json.getAsDouble();
            else result.stringValue = json.getAsString();
        }
        else result.numericalValue = constant(value);
        if (result.numericalValue != null
                && (Double.compare(result.numericalValue, -Double.MAX_VALUE) < 0 || Double.compare(result.numericalValue, Double.MAX_VALUE) > 0)
        ) {
            throw new IllegalArgumentException("Molang numbers must be finite");
        }
        return result;
    }

    @NotNull
    public static IValue copy(@NotNull IValue value, @NotNull MolangParser parser) {
        if (value instanceof MolangExpression expression) {
            RawMolangValue raw = fromValue(expression);
            return raw.compile(parser);
        }
        if (value instanceof Constant || value instanceof ConstantValue) return new Constant(constant(value));
        // Java callbacks remain local.
        return value;
    }

    @Nullable
    public static IValue[] copy(@NotNull IValue @Nullable [] values, @NotNull MolangParser parser) {
        if (values == null) return null;
        IValue[] result = new IValue[values.length];
        for (int i = 0; i < values.length; i++) result[i] = copy(values[i], parser);
        return result;
    }

    @NotNull
    public static MolangExpression compileExpression(@NotNull String expression) {
        RawMolangValue raw = new RawMolangValue();
        raw.stringValue = Objects.requireNonNull(expression);
        return raw.compile(new MolangParser());
    }

    public static double constant(@NotNull IValue value) {
        if (value instanceof Constant || value instanceof ConstantValue
                || value instanceof MolangExpression expression && MolangExpression.isExpressionConstant(expression)
        ) {
            double number = value.get();
            if (number >= -Double.MAX_VALUE && number <= Double.MAX_VALUE) return number;
            throw new IllegalArgumentException("Molang numbers must be finite");
        }
        throw new IllegalArgumentException("Expected a numerical constant");
    }

    public static class Deserializer implements JsonDeserializer<RawMolangValue> {
        @Override
        public RawMolangValue deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            RawMolangValue toReturn = new RawMolangValue();
            if (!json.isJsonPrimitive()) throw new JsonParseException("Expected numerical value or string in " + typeOfT.getTypeName());

            JsonPrimitive primitive = json.getAsJsonPrimitive();

            //numerical value
            if (primitive.isNumber()) toReturn.numericalValue = primitive.getAsDouble();
            //string value
            else if (primitive.isString()) toReturn.stringValue = primitive.getAsString();
            //exception when otherwise
            else throw new JsonParseException("Expected numerical value or string in " + typeOfT.getTypeName());

            return toReturn;
        }
    }
}
