package anightdazingzoroark.riftlib.jsonParsing.raw.geo;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RawModelBoundingBoxList {
    public List<RawBoundingBox> list = new ArrayList<>();

    public static class RawBoundingBox {
        public String name;
        public double[] origin = new double[]{0, 0, 0};
        public double[] size = new double[]{1D, 1D};
        public boolean collision;
        public String[] tags = {};
    }

    public static class Deserializer implements JsonDeserializer<RawModelBoundingBoxList> {
        @Override
        public RawModelBoundingBoxList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            RawModelBoundingBoxList toReturn = new RawModelBoundingBoxList();

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                JsonElement element = entry.getValue();

                RawBoundingBox rawBoundingBox = new RawBoundingBox();
                rawBoundingBox.name = entry.getKey();

                if (!element.isJsonObject()) throw new JsonParseException("Unexpected type for "+entry.getKey()+": " + json.toString());

                JsonObject object = element.getAsJsonObject();

                if (object.has("origin")) {
                    rawBoundingBox.origin = context.deserialize(object.get("origin"), double[].class);
                }

                if (object.has("size")) {
                    rawBoundingBox.size = context.deserialize(object.get("size"), double[].class);
                }

                if (object.has("collision")) {
                    rawBoundingBox.collision = object.get("collision").getAsBoolean();
                }

                if (object.has("tags")) {
                    rawBoundingBox.tags = context.deserialize(object.get("tags"), String[].class);
                }

                toReturn.list.add(rawBoundingBox);
            }

            return toReturn;
        }
    }
}
