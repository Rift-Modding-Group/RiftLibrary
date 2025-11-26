package anightdazingzoroark.riftlib.newGeo.raw;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RawGeoModel {
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
        public int texture_width;

        @SerializedName("texture_height")
        public int texture_height;

        @SerializedName("visible_bounds_width")
        public double visible_bounds_width;

        @SerializedName("visible_bounds_height")
        public double visible_bounds_height;

        @SerializedName("visible_bounds_offset")
        public double[] visible_bounds_offset;
    }

    public static class RawModelBone {
        @SerializedName("name")
        public String name;

        @SerializedName("parent")
        public String parent;

        @SerializedName("pivot")
        public double[] pivot;

        @SerializedName("rotation")
        public double[] rotation;

        @SerializedName("inflate")
        public double inflate;

        @SerializedName("mirror")
        public boolean mirror;

        @SerializedName("cubes")
        public List<RawModelCube> cubes;

        @SerializedName("locators")
        public RawModelLocatorList locators;
    }

    public static class RawModelCube {
        @SerializedName("origin")
        public double[] origin;

        @SerializedName("pivot")
        public double[] pivot;

        @SerializedName("rotation")
        public double[] rotation;

        @SerializedName("size")
        public double[] size;

        @SerializedName("inflate")
        public double inflate;

        @SerializedName("mirror")
        public boolean mirror;

        @SerializedName("uv")
        public int[] uv;
    }

    public static class RawModelLocatorList {}
}
