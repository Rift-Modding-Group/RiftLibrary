package anightdazingzoroark.riftlib.geo;

import anightdazingzoroark.riftlib.geo.raw.RawGeoModel;
import anightdazingzoroark.riftlib.geo.raw.RawUVUnion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ModelConverter {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RawUVUnion.class, new RawUVUnion.Deserializer())
            .create();

    public static RawGeoModel convertModelJSONToRawGeoModel(String json) {
        return gson.fromJson(json, RawGeoModel.class);
    }
}
