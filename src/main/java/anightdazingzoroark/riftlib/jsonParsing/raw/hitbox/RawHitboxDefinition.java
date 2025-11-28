package anightdazingzoroark.riftlib.jsonParsing.raw.hitbox;

import com.google.gson.annotations.SerializedName;

public class RawHitboxDefinition {
    @SerializedName("hitboxes")
    public RawHitbox[] hitboxes;

    public static class RawHitbox {
        @SerializedName("locator")
        public String locator;

        @SerializedName("width")
        public Float width;

        @SerializedName("height")
        public Float height;

        @SerializedName("damageMultiplier")
        public Float damageMultiplier;

        @SerializedName("affectedByAnim")
        public Boolean affectedByAnim;

        @SerializedName("damageDefinitions")
        public RawHitboxDamageDefinition[] damageDefinitions;
    }

    public static class RawHitboxDamageDefinition {
        @SerializedName("damageSource")
        public String damageSource;

        @SerializedName("damageType")
        public String damageType;

        @SerializedName("damageMultiplier")
        public Float damageMultiplier;
    }
}
