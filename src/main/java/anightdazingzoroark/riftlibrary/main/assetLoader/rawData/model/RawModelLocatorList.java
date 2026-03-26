package anightdazingzoroark.riftlibrary.main.assetLoader.rawData.model;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RawModelLocatorList {
    public List<RawModelLocator> list = new ArrayList<>();

    public static class RawModelLocator {
        public String name;
        public float[] offset = new float[]{0, 0, 0};
        public float[] rotation = new float[]{0, 0, 0};
    }

    public static class Deserialize implements JsonDeserializer<RawModelLocatorList> {
        @Override
        public RawModelLocatorList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            RawModelLocatorList toReturn = new RawModelLocatorList();

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                RawModelLocator rawModelLocator = new RawModelLocator();
                JsonElement element = entry.getValue();

                //if its just an array, set that as the offset
                if (element.isJsonArray()) {
                    rawModelLocator.name = entry.getKey();
                    rawModelLocator.offset = context.deserialize(element, float[].class);
                }
                //otherwise, it must be an object, so read it up
                else if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();

                    rawModelLocator.name = entry.getKey();

                    if (object.has("offset")) {
                        rawModelLocator.offset = context.deserialize(object.get("offset"), float[].class);
                    }

                    if (object.has("rotation")) {
                        rawModelLocator.rotation = context.deserialize(object.get("rotation"), float[].class);
                    }
                }
                else throw new JsonParseException("Unexpected type: " + json.toString());
                toReturn.list.add(rawModelLocator);
            }

            return toReturn;
        }
    }
}
