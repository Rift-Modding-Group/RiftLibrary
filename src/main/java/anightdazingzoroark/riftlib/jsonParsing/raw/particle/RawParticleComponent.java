package anightdazingzoroark.riftlib.jsonParsing.raw.particle;

import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.molang.math.IValue;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RawParticleComponent {
    public Map<String, ComponentValue> componentValues;

    public RawParticleComponent() {
    }


    @NotNull
    public static ComponentValue fromValue(@NotNull Object input) {
        return switch (input) {
            case RawParticleComponent.ComponentValue component -> switch (component) {
                case RawParticleComponent.MolangValue(RawMolangValue raw) -> fromValue(raw);
                case RawParticleComponent.BooleanValue(boolean bool) -> new RawParticleComponent.BooleanValue(bool);
                case RawParticleComponent.ArrayValue(List<RawParticleComponent.ComponentValue> values) ->
                        fromValue(values);
                case RawParticleComponent.ObjectValue(Map<String, RawParticleComponent.ComponentValue> values) ->
                        fromValue(values);
            };
            case Boolean bool -> new RawParticleComponent.BooleanValue(bool);
            case Map<?, ?> map -> {
                Map<String, RawParticleComponent.ComponentValue> values = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet())
                    values.put((String) entry.getKey(), fromValue(entry.getValue()));
                yield new RawParticleComponent.ObjectValue(values);
            }
            case Object[] array -> fromValue(List.of(array));
            case List<?> list -> {
                List<RawParticleComponent.ComponentValue> values = new ArrayList<>();
                for (Object element : list) values.add(fromValue(element));
                yield new RawParticleComponent.ArrayValue(values);
            }
            case RawMolangValue molang -> {
                RawMolangValue raw = new RawMolangValue();
                raw.stringValue = molang.stringValue;
                raw.numericalValue = molang.numericalValue;
                if (raw.numericalValue != null && (Double.compare(raw.numericalValue, -Double.MAX_VALUE) < 0 || Double.compare(raw.numericalValue, Double.MAX_VALUE) > 0)) {
                    throw new IllegalArgumentException("Particle numbers must be finite");
                }
                if (raw.stringValue == null && raw.numericalValue == null)
                    throw new IllegalArgumentException("Empty Molang value");
                yield new RawParticleComponent.MolangValue(raw);
            }
            case IValue number -> fromValue(RawMolangValue.fromValue(number));
            case Number number -> {
                RawMolangValue raw = new RawMolangValue();
                raw.numericalValue = number.doubleValue();
                yield fromValue(raw);
            }
            case String string -> {
                RawMolangValue raw = new RawMolangValue();
                raw.stringValue = string;
                yield fromValue(raw);
            }
            default -> throw new IllegalArgumentException("Unsupported particle value: " + input.getClass().getName());
        };
    }

    //for debugging lol
    @Override
    public String toString() {
        if (this.componentValues != null) return this.componentValues.toString();
        return "";
    }

    /**
     * Typed values used by the built-in particle and emitter components.
     *
     */
    public sealed interface ComponentValue permits MolangValue, BooleanValue, ArrayValue, ObjectValue {
        @NotNull
        default RawMolangValue asMolang() {
            if (this instanceof MolangValue(RawMolangValue value)) return value;
            throw new IllegalArgumentException("Expected a number or Molang expression, got " + this);
        }

        default double asNumber() {
            if (this instanceof MolangValue(RawMolangValue value) && value.numericalValue != null) {
                return value.numericalValue;
            }
            throw new IllegalArgumentException("Expected a number, got " + this);
        }

        @NotNull
        default String asString() {
            if (this instanceof MolangValue(RawMolangValue value) && value.stringValue != null) {
                return value.stringValue;
            }
            throw new IllegalArgumentException("Expected a string, got " + this);
        }

        default boolean asBoolean() {
            if (this instanceof BooleanValue(boolean value)) return value;
            throw new IllegalArgumentException("Expected a boolean, got " + this);
        }

        @NotNull
        default List<ComponentValue> asArray() {
            if (this instanceof ArrayValue(List<ComponentValue> values)) return values;
            throw new IllegalArgumentException("Expected an array, got " + this);
        }

        @NotNull
        default Map<String, ComponentValue> asObject() {
            if (this instanceof ObjectValue(Map<String, ComponentValue> values)) return values;
            throw new IllegalArgumentException("Expected an object, got " + this);
        }

        @NotNull
        default RawMolangValue[] asMolangArray(int expectedSize) {
            List<ComponentValue> values = this.asArray();
            if (values.size() != expectedSize) {
                throw new IllegalArgumentException("Expected " + expectedSize + " array elements, got " + values.size());
            }
            RawMolangValue[] result = new RawMolangValue[expectedSize];
            for (int i = 0; i < expectedSize; i++) result[i] = values.get(i).asMolang();
            return result;
        }

        @NotNull
        default JsonElement toJson() {
            return switch (this) {
                case RawParticleComponent.MolangValue(RawMolangValue raw) -> {
                    if (raw.stringValue != null) yield new JsonPrimitive(raw.stringValue);
                    yield new JsonPrimitive(Objects.requireNonNull(raw.numericalValue, "Empty Molang value"));
                }
                case RawParticleComponent.BooleanValue(boolean bool) -> new JsonPrimitive(bool);
                case RawParticleComponent.ArrayValue(List<RawParticleComponent.ComponentValue> values) -> {
                    JsonArray array = new JsonArray();
                    for (RawParticleComponent.ComponentValue element : values) array.add(element.toJson());
                    yield array;
                }
                case RawParticleComponent.ObjectValue(Map<String, RawParticleComponent.ComponentValue> values) -> {
                    JsonObject object = new JsonObject();
                    for (Map.Entry<String, RawParticleComponent.ComponentValue> entry : values.entrySet()) {
                        object.add(entry.getKey(), entry.getValue().toJson());
                    }
                    yield object;
                }
            };
        }

        default boolean isString() {
            return this instanceof MolangValue(RawMolangValue value) && value.stringValue != null;
        }

        default boolean isArray() {
            return this instanceof ArrayValue;
        }
    }

    public record MolangValue(@NotNull RawMolangValue value) implements ComponentValue {
    }

    public record BooleanValue(boolean value) implements ComponentValue {
    }

    public record ArrayValue(@NotNull List<ComponentValue> values) implements ComponentValue {
    }

    public record ObjectValue(@NotNull Map<String, ComponentValue> values) implements ComponentValue {
    }

    public static class Deserializer implements JsonDeserializer<RawParticleComponent> {
        @Override
        public RawParticleComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            RawParticleComponent toReturn = new RawParticleComponent();
            toReturn.componentValues = new LinkedHashMap<>();

            if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                    toReturn.componentValues.put(entry.getKey(), this.parseValue(entry.getValue(), context));
                }
            } else {
                //if the component is not an object and is just a primitive,
                //the only ComponentValue is gonna be named "$value"
                toReturn.componentValues.put("$value", this.parseValue(json, context));
            }

            return toReturn;
        }

        @NotNull
        private ComponentValue parseValue(@NotNull JsonElement element, @NotNull JsonDeserializationContext context) {
            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isBoolean()) return new BooleanValue(element.getAsBoolean());
                return new MolangValue(context.deserialize(element, RawMolangValue.class));
            } else if (element.isJsonArray()) {
                List<ComponentValue> values = new ArrayList<>();
                for (JsonElement child : element.getAsJsonArray()) {
                    values.add(this.parseValue(child, context));
                }
                return new ArrayValue(values);
            } else if (element.isJsonObject()) {
                Map<String, ComponentValue> values = new LinkedHashMap<>();
                JsonObject obj = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                    values.put(entry.getKey(), this.parseValue(entry.getValue(), context));
                }
                return new ObjectValue(values);
            } else throw new JsonParseException("Unknown JsonElement type: " + element);
        }
    }
}
