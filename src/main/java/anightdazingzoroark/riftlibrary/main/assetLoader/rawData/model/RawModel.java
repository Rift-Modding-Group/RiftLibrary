package anightdazingzoroark.riftlibrary.main.assetLoader.rawData.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RawModel {
    @SerializedName("format_version")
    public String format_version;

    @SerializedName("minecraft:geometry")
    public List<MinecraftGeometry> geometry;

    public static class MinecraftGeometry {
        @SerializedName("description")
        public RawModelDescription description;

        @SerializedName("bones")
        public List<RawModelBone> bones;
    }

    public static class RawModelDescription {
        @SerializedName("identifier")
        public String identifier;

        @SerializedName("texture_width")
        public Integer texture_width;

        @SerializedName("texture_height")
        public Integer texture_height;

        @SerializedName("visible_bounds_width")
        public Float visible_bounds_width;

        @SerializedName("visible_bounds_height")
        public Float visible_bounds_height;

        @SerializedName("visible_bounds_offset")
        public float[] visible_bounds_offset;
    }

    public static class RawModelBone {
        @SerializedName("name")
        public String name;

        @SerializedName("parent")
        public String parent;

        @SerializedName("pivot")
        public float[] pivot = new float[]{0, 0, 0};

        @SerializedName("rotation")
        public float[] rotation = new float[]{0, 0, 0};

        @SerializedName("inflate")
        public Float inflate;

        @SerializedName("mirror")
        public Boolean mirror;

        @SerializedName("cubes")
        public List<RawModelCube> cubes;

        @SerializedName("locators")
        public RawModelLocatorList locators;
    }

    public static class RawModelCube {
        @SerializedName("origin")
        public float[] origin;

        @SerializedName("pivot")
        public float[] pivot = new float[]{0, 0, 0};

        @SerializedName("rotation")
        public float[] rotation = new float[]{0, 0, 0};

        @SerializedName("size")
        public float[] size = new float[]{0, 0, 0};

        @SerializedName("inflate")
        public Float inflate;

        @SerializedName("mirror")
        public Boolean mirror;

        @SerializedName("uv")
        public RawUVUnion uv;
    }
}
