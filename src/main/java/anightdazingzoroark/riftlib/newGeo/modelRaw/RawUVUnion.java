package anightdazingzoroark.riftlib.newGeo.modelRaw;

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
            RawUVUnion result = new RawUVUnion();

            if (json.isJsonArray()) {
                result.boxUV = context.deserialize(json, int[].class);
            }
            else if (json.isJsonObject()) {
                result.faceUV = context.deserialize(json, RawFaceUVUser.class);
            }
            else throw new JsonParseException("Unexpected type: " + json.toString());

            return result;
        }
    }
}
