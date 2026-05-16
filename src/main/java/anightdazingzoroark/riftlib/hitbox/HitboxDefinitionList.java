package anightdazingzoroark.riftlib.hitbox;

import java.util.ArrayList;
import java.util.List;

public class HitboxDefinitionList {
    public final List<HitboxDefinition> list = new ArrayList<>();

    public record HitboxDefinition(String locator, float width, float height, float damageMultiplier,
                                   boolean affectedByAnim, List<HitboxDamageDefinition> damageDefinitionList) {

        //its here just to make debugging easier
            @Override
            public String toString() {
                return "[locator=" + this.locator + ", size=(" + this.width + ", " + this.height + "), damageMultiplier=" + this.damageMultiplier + ", affectedByAnim=" + this.affectedByAnim + "]";
            }
        }

    public record HitboxDamageDefinition(String damageSource, String damageType, float damageMultiplier) {
        //either one of damageSource or damageType must be null

        //its here just to make debugging easier
            @Override
            public String toString() {
                return "[source=" + this.damageSource + ", type=" + this.damageType + ", multiplier=" + this.damageMultiplier + "]";
            }
        }
}
