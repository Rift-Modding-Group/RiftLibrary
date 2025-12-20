package anightdazingzoroark.riftlib.jsonParsing.raw.geo;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RawModelLocatorList {
    public List<RawModelLocator> list = new ArrayList<>();

    public static class RawModelLocator {
        public String name;
        public double[] offset = new double[]{0, 0, 0};
        public double[] rotation = new double[]{0, 0, 0};
    }

    public static class Deserialize implements JsonDeserializer<RawModelLocatorList> {
        @Override
        public RawModelLocatorList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            RawModelLocatorList toReturn = new RawModelLocatorList();

            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                RawModelLocator rawModelLocator = new RawModelLocator();
                JsonElement element = entry.getValue();

                //if its just an array, set that as the offset
                if (element.isJsonArray()) {
                    rawModelLocator.name = entry.getKey();
                    rawModelLocator.offset = context.deserialize(element, double[].class);

                    System.out.println("rawModelLocator.name: "+rawModelLocator.name);
                    System.out.println("rawModelLocator.offset: "+ Arrays.toString(rawModelLocator.offset));
                }
                //otherwise, it must be an object, so read it up
                else if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();

                    rawModelLocator.name = entry.getKey();

                    if (object.has("offset")) {
                        rawModelLocator.offset = context.deserialize(object.get("offset"), double[].class);
                    }

                    if (object.has("rotation")) {
                        rawModelLocator.rotation = context.deserialize(object.get("rotation"), double[].class);
                    }
                }
                else throw new JsonParseException("Unexpected type: " + json.toString());
                toReturn.list.add(rawModelLocator);
            }

            return toReturn;
        }
    }
}
