package anightdazingzoroark.riftlib.newGeo.hitboxRaw;

import com.google.gson.annotations.SerializedName;

public class RawHitboxDefinitions {
    @SerializedName("hitboxes")
    public RawHitbox[] hitboxes;

    public static class RawHitbox {
        @SerializedName("locator")
        private String locator;

        @SerializedName("width")
        private Double width;

        @SerializedName("height")
        private Double height;

        @SerializedName("damageMultiplier")
        private Double damageMultiplier;

        @SerializedName("affectedByAnim")
        private Boolean affectedByAnim;

        @SerializedName("rawHitboxDamageDefinitions")
        private RawHitboxDamageDefinitions[] rawHitboxDamageDefinitions;
    }

    public static class RawHitboxDamageDefinitions {
        @SerializedName("damageSource")
        private String damageSource;

        @SerializedName("damageType")
        private String damageType;

        @SerializedName("damageMultiplier")
        private double damageMultiplier;
    }
}
