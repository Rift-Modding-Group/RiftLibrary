package anightdazingzoroark.riftlib.file;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HitboxDefinitionList {
    public final List<HitboxDefinition> list = new ArrayList<>();

    public HitboxDefinition getHitboxDefinitionByName(String name) {
        List<HitboxDefinition> filteredList = this.list.stream().filter(h -> h.locator.equals(name))
                .collect(Collectors.toList());
        if (!filteredList.isEmpty()) return filteredList.get(0);
        return null;
    }

    public void editHitboxDefinitionPosition(String name, float x, float y, float z) {
        HitboxDefinition hitboxDefinitionToEdit = this.getHitboxDefinitionByName(name);
        this.list.remove(hitboxDefinitionToEdit);
        hitboxDefinitionToEdit.position = new Vec3d(x, y, z);
        this.list.add(hitboxDefinitionToEdit);
    }

    public static class HitboxDefinition {
        public final String locator;
        public final float width;
        public final float height;
        public final float damageMultiplier;
        public final boolean affectedByAnim;
        public final List<HitboxDamageDefinition> damageDefinitionList;
        public Vec3d position = new Vec3d(0f, 0f, 0f);

        public HitboxDefinition(String locator, float width, float height, float damageMultiplier, boolean affectedByAnim, List<HitboxDamageDefinition> damageDefinitionList) {
            this.locator = locator;
            this.width = width;
            this.height = height;
            this.damageMultiplier = damageMultiplier;
            this.affectedByAnim = affectedByAnim;
            this.damageDefinitionList = damageDefinitionList;
        }

        //its here just to make debugging easier
        @Override
        public String toString() {
            return "[locator="+this.locator+", size=("+this.width+", "+this.height+"), position=("+this.position.x+", "+this.position.y+", "+this.position.z+"), damageMultiplier="+this.damageMultiplier+", affectedByAnim="+this.affectedByAnim+"]";
        }
    }

    public static class HitboxDamageDefinition {
        public final String damageSource;
        public final String damageType;
        public final float damageMultiplier;

        public HitboxDamageDefinition(String damageSource, String damageType, float damageMultiplier) {
            //either one of damageSource or damageType must be null
            this.damageSource = damageSource;
            this.damageType = damageType;
            this.damageMultiplier = damageMultiplier;
        }

        //its here just to make debugging easier
        @Override
        public String toString() {
            return "[source="+this.damageSource+", type="+this.damageType+", multiplier="+this.damageMultiplier+"]";
        }
    }
}
