package anightdazingzoroark.riftlib.jsonParsing.raw.animation;

import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.*;

public class RawAnimationChannel {
    //keyframes must be a list, and entries must be ordered based on time
    //explicit vectors also go here, they are placed in 0 by default
    public List<RawKeyframe> keyframes;

    public boolean isKeyframed() {
        return this.keyframes != null;
    }

    public static class RawKeyframe {
        //this is primitive because by default, timestamp of keyframe is gonna be 0
        public double time;
        //note that vectors are by strings because of the existence of molang
        public RawMolangValue[] vector;
        public String easingType;
        //i have no idea what this does cos i couldn't find use cases not even in GeckoLib's
        //old example files but well
        public double[] easingArgs;
    }

    public static class Deserializer implements JsonDeserializer<RawAnimationChannel> {
        @Override
        public RawAnimationChannel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            RawAnimationChannel toReturn = new RawAnimationChannel();

            //is vector (as array or singular)
            if (json.isJsonArray() || json.isJsonPrimitive()) {
                RawKeyframe initRawKeyframe = new RawKeyframe();
                initRawKeyframe.time = 0.0D;
                initRawKeyframe.vector = this.deserializeVector(json, context);

                toReturn.keyframes = Collections.singletonList(initRawKeyframe);
            }
            //is keyframes
            else if (json.isJsonObject()) {
                List<RawKeyframe> frames = new ArrayList<>();
                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                    RawKeyframe rawKeyframe = new RawKeyframe();

                    //sometimes, a frame may just be a declaration of a vector, hence this if/else
                    //start with if its its a keyframe
                    if (this.isDoubleOnly(entry.getKey())) {
                        //start with getting time frame
                        try {
                            rawKeyframe.time = Double.parseDouble(entry.getKey());
                        }
                        catch (NumberFormatException e) {
                            throw new JsonParseException("Invalid keyframe time: " + entry.getKey(), e);
                        }

                        //it just a vector (as array or singular)
                        if (entry.getValue().isJsonArray() || entry.getValue().isJsonPrimitive()) {
                            rawKeyframe.vector = this.deserializeVector(entry.getValue(), context);
                        }
                        //explicit vector declaration
                        else {
                            rawKeyframe.vector = this.deserializeVector(entry.getValue().getAsJsonObject().get("vector"), context);
                        }

                        //easing type stuff
                        if (entry.getValue().isJsonObject() && entry.getValue().getAsJsonObject().has("easing")) {
                            rawKeyframe.easingType = entry.getValue().getAsJsonObject().get("easing").getAsString();
                        }

                        //easing args stuff
                        if (entry.getValue().isJsonObject() &&entry.getValue().getAsJsonObject().has("easingArgs")) {
                            rawKeyframe.easingArgs = context.deserialize(entry.getValue().getAsJsonObject().get("easingArgs"), double[].class);
                        }
                    }
                    //now for if its an explicit vector declaration
                    else rawKeyframe.vector = this.deserializeVector(entry.getValue(), context);

                    frames.add(rawKeyframe);
                }

                //order all elements in entry by timeframe
                frames.sort(new Comparator<RawKeyframe>() {
                    @Override
                    public int compare(RawKeyframe a, RawKeyframe b) {
                        return Double.compare(a.time, b.time);
                    }
                });

                toReturn.keyframes = frames;
            }
            else throw new JsonParseException("Unexpected type: " + json.toString());

            return toReturn;
        }

        private RawMolangValue[] deserializeVector(JsonElement element, JsonDeserializationContext context) {
            RawMolangValue[] toReturn = new RawMolangValue[3];

            //take note that there will be times where the vector is just 1 singular element
            //in that case, the vector is the same value on all axes
            //start with normal array case
            if (element.isJsonArray()) {
                JsonArray jsonArray = element.getAsJsonArray();

                for (int i = 0; i < toReturn.length; i++) {
                    JsonElement elementInJsonArray = jsonArray.get(i);
                    RawMolangValue vectorValue = context.deserialize(elementInJsonArray, RawMolangValue.class);
                    toReturn[i] = vectorValue;
                }

            }
            //now with singular case
            else {
                RawMolangValue vectorValue = context.deserialize(element, RawMolangValue.class);
                Arrays.fill(toReturn, vectorValue);

            }
            return toReturn;
        }

        private boolean isDoubleOnly(String string) {
            if (string == null) return false;
            try {
                Double.parseDouble(string);
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
    }
}
