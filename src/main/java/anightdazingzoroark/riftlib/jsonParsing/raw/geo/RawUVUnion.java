package anightdazingzoroark.riftlib.jsonParsing.raw.geo;

import com.google.gson.*;

import java.lang.reflect.Type;

public class RawUVUnion {
    public int[] boxUV;
    public RawFaceUVUser faceUV;

    public boolean isBoxUV() {
        return this.boxUV != null;
    }

    public boolean isFaceUV() {
        return this.faceUV != null;
    }

    public static class Deserializer implements JsonDeserializer<RawUVUnion> {
        @Override
        public RawUVUnion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            RawUVUnion toReturn = new RawUVUnion();

            //is box uv
            if (json.isJsonArray()) {
                toReturn.boxUV = context.deserialize(json, int[].class);
            }
            //is face uv
            else if (json.isJsonObject()) {
                toReturn.faceUV = context.deserialize(json, RawFaceUVUser.class);
            }
            else throw new JsonParseException("Unexpected type: " + json.toString());

            return toReturn;
        }
    }
}
