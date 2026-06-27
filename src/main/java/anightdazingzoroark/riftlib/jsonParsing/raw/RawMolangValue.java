package anightdazingzoroark.riftlib.jsonParsing.raw;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Made to help out with json fields that are either
 * a molang expression or a numerical value
 * */
public class RawMolangValue {
    public String stringValue;
    public Double numericalValue;

    public static class Deserializer implements JsonDeserializer<RawMolangValue> {
        @Override
        public RawMolangValue deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            RawMolangValue toReturn = new RawMolangValue();
            if (!json.isJsonPrimitive()) throw new JsonParseException("Expected numerical value or string in "+typeOfT.getTypeName());

            JsonPrimitive primitive = json.getAsJsonPrimitive();

            //numerical value
            if (primitive.isNumber()) toReturn.numericalValue = primitive.getAsDouble();
            //string value
            else if (primitive.isString()) toReturn.stringValue = primitive.getAsString();
            //exception when otherwise
            else throw new JsonParseException("Expected numerical value or string in "+typeOfT.getTypeName());

            return toReturn;
        }
    }
}
