package anightdazingzoroark.riftlib.newGeo;

import anightdazingzoroark.riftlib.newGeo.modelRaw.RawGeoModel;
import anightdazingzoroark.riftlib.newGeo.modelRaw.RawUVUnion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Converter {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RawUVUnion.class, new RawUVUnion.Deserializer())
            .create();

    public static RawGeoModel convertModelJSONToRawGeoModel(String json) {
        return gson.fromJson(json, RawGeoModel.class);
    }
}
