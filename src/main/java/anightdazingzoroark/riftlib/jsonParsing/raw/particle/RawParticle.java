package anightdazingzoroark.riftlib.jsonParsing.raw.particle;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class RawParticle {
    @SerializedName("particle_effect")
    public RawParticleEffect rawParticleEffect;

    public static class RawParticleEffect {
        @SerializedName("description")
        public RawParticleDescription description;

        @SerializedName("components") //key is name of component, value is the stuff in the component object
        public Map<String, RawParticleComponent> components;
    }

    public static class RawParticleDescription {
        @SerializedName("identifier")
        public String identifier;

        @SerializedName("basic_render_parameters")
        public RawBasicRenderParameters basicRenderParameters;
    }

    public static class RawBasicRenderParameters {
        @SerializedName("material")
        public String material;

        @SerializedName("texture")
        public String texture;
    }
}
