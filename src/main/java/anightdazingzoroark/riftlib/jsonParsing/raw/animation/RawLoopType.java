package anightdazingzoroark.riftlib.jsonParsing.raw.animation;

import com.google.gson.*;

import java.lang.reflect.Type;

public class RawLoopType {
    public boolean holdOnLastFrame;
    public boolean loop;

    public static class Deserializer implements JsonDeserializer<RawLoopType> {
        @Override
        public RawLoopType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            RawLoopType toReturn = new RawLoopType();

            if (json.isJsonPrimitive()) {
                JsonPrimitive primitive = json.getAsJsonPrimitive();
                if (primitive.getAsString().equals("hold_on_last_frame")) {
                    toReturn.holdOnLastFrame = true;
                }
                else if (primitive.isBoolean()) {
                    toReturn.loop = primitive.getAsBoolean();
                }
            }
            else throw new JsonParseException("Expected boolean or string!");

            return toReturn;
        }
    }
}
