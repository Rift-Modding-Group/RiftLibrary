package anightdazingzoroark.riftlib.jsonParsing.raw.animation;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class RawAnimationFile {
    @SerializedName("animations")
    public Map<String, RawAnimation> rawAnimations;

    public static class RawAnimation {
        @SerializedName("loop")
        public Boolean loop;

        @SerializedName("animation_length")
        public Double animationLength;

        @SerializedName("bones")
        public Map<String, RawBoneAnimations> bones;
    }

    public static class RawBoneAnimations {
        @SerializedName("rotation")
        public RawAnimationChannel rotation;

        @SerializedName("scale")
        public RawAnimationChannel scale;

        @SerializedName("position")
        public RawAnimationChannel position;
    }
}
