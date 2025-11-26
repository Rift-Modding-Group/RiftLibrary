package anightdazingzoroark.riftlib.geo.raw;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

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
        public Integer texture_width;

        @SerializedName("texture_height")
        public Integer texture_height;

        @SerializedName("visible_bounds_width")
        public Double visible_bounds_width;

        @SerializedName("visible_bounds_height")
        public Double visible_bounds_height;

        @SerializedName("visible_bounds_offset")
        public double[] visible_bounds_offset;
    }

    public static class RawModelBone {
        @SerializedName("name")
        public String name;

        @SerializedName("parent")
        public String parent;

        @SerializedName("pivot")
        public double[] pivot = new double[]{0, 0, 0};

        @SerializedName("rotation")
        public double[] rotation = new double[]{0, 0, 0};

        @SerializedName("inflate")
        public Double inflate;

        @SerializedName("mirror")
        public Boolean mirror;

        @SerializedName("cubes")
        public List<RawModelCube> cubes;

        @SerializedName("locators")
        public Map<String, double[]> locators;
    }

    public static class RawModelCube {
        @SerializedName("origin")
        public double[] origin;

        @SerializedName("pivot")
        public double[] pivot = new double[]{0, 0, 0};

        @SerializedName("rotation")
        public double[] rotation = new double[]{0, 0, 0};

        @SerializedName("size")
        public double[] size = new double[]{0, 0, 0};

        @SerializedName("inflate")
        public Double inflate;

        @SerializedName("mirror")
        public Boolean mirror;

        @SerializedName("uv")
        public RawUVUnion uv;
    }

    public static class RawModelLocatorList {}
}
