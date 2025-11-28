package anightdazingzoroark.riftlib.jsonParsing.raw.geo;

import com.google.gson.annotations.SerializedName;

public class RawFaceUVUser {
    @SerializedName("north")
    public RawUVFace northUV;

    @SerializedName("east")
    public RawUVFace eastUV;

    @SerializedName("south")
    public RawUVFace southUV;

    @SerializedName("west")
    public RawUVFace westUV;

    @SerializedName("up")
    public RawUVFace upUV;

    @SerializedName("down")
    public RawUVFace downUV;

    public static class RawUVFace {
        @SerializedName("uv")
        public int[] uv;

        @SerializedName("uv_size")
        public int[] uv_size;
    }
}
