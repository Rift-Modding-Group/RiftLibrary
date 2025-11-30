package anightdazingzoroark.riftlib.jsonParsing.raw.particle;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RawParticleComponent {
    public Map<String, ComponentValue> componentValues;

    //for debugging lol
    @Override
    public String toString() {
        if (this.componentValues != null) return this.componentValues.toString();
        return "";
    }

    public static class ComponentValue {
        public ComponentValueType valueType;
        public Float number; //must be a number
        public String string; //must be a molang expression
        public Boolean bool;
        public List<ComponentValue> array; //must be an array of anything
        public Map<String, ComponentValue> object; //must be an object with more values

        //for debugging lol
        @Override
        public String toString() {
            switch (this.valueType) {
                case NUMBER:
                    return this.number.toString();
                case STRING:
                    return this.string;
                case BOOLEAN:
                    return this.bool.toString();
                case ARRAY:
                    return this.array.toString();
                case OBJECT:
                    return this.object.toString();
            }
            return "NULL";
        }
    }

    public static enum ComponentValueType {
        NUMBER,
        STRING,
        BOOLEAN,
        ARRAY,
        OBJECT
    }

    public static class Deserializer implements JsonDeserializer<RawParticleComponent> {
        @Override
        public RawParticleComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            RawParticleComponent toReturn = new RawParticleComponent();
            toReturn.componentValues = new LinkedHashMap<>();

            if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                    toReturn.componentValues.put(entry.getKey(), parseValue(entry.getValue()));
                }
            }
            else {
                //if the component is not an object and is just a primitive,
                //the only ComponentValue is gonna be named "$value"
                toReturn.componentValues.put("$value", parseValue(json));
            }

            return toReturn;
        }

        private ComponentValue parseValue(JsonElement element) {
            ComponentValue toReturn = new ComponentValue();

            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isNumber()) {
                    toReturn.valueType = ComponentValueType.NUMBER;
                    toReturn.number = element.getAsJsonPrimitive().getAsFloat();
                }
                else if (element.getAsJsonPrimitive().isBoolean()) {
                    toReturn.valueType = ComponentValueType.BOOLEAN;
                    toReturn.bool = element.getAsJsonPrimitive().getAsBoolean();
                }
                else {
                    toReturn.valueType = ComponentValueType.STRING;
                    toReturn.string = element.getAsJsonPrimitive().getAsString();
                }
            }
            else if (element.isJsonArray()) {
                toReturn.valueType = ComponentValueType.ARRAY;
                toReturn.array = new ArrayList<>();
                for (JsonElement child : element.getAsJsonArray()) {
                    toReturn.array.add(this.parseValue(child));
                }
            }
            else if (element.isJsonObject()) {
                toReturn.valueType = ComponentValueType.OBJECT;
                toReturn.object = new LinkedHashMap<>();
                JsonObject obj = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                    toReturn.object.put(entry.getKey(), this.parseValue(entry.getValue()));
                }
            }
            else throw new JsonParseException("Unknown JsonElement type: " + element);
            return toReturn;
        }
    }
}
