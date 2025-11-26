package anightdazingzoroark.riftlib.newGeo;

import anightdazingzoroark.riftlib.newGeo.raw.RawGeoModel;
import com.google.gson.Gson;

public class Converter {
    private static final Gson gson = new Gson();

    public static RawGeoModel convertModelJSONToRawGeoModel(String json) {
        return gson.fromJson(json, RawGeoModel.class);
    }
}
