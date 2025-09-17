package anightdazingzoroark.riftlib.mobFamily;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MobFamilyHelper {
    //todo: make this return all mob families this entity is in
    public static List<String> getMobFamilies(Entity entity) {
        String entityId = EntityList.getEntityString(entity);
        return MobFamilyStorage.MOB_FAMILIES.stream()
                .filter(m -> m.getFamilyMembers().contains(entityId))
                .map(MobFamily::getName)
                .collect(Collectors.toList());
    }

    public static boolean entityInMobFamily(Entity entity, String mobFamilyName) {
        for (MobFamily mobFamily : MobFamilyStorage.MOB_FAMILIES) {
            if (mobFamily.getName().equals(mobFamilyName)) {
                String entityId = EntityList.getEntityString(entity);
                if (mobFamily.getFamilyMembers().contains(entityId)) return true;
            }
        }
        return false;
    }
}
