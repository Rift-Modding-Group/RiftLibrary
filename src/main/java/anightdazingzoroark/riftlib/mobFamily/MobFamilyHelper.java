package anightdazingzoroark.riftlib.mobFamily;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;
import java.util.stream.Collectors;

public class MobFamilyHelper {
    public static List<String> getMobFamilyNames(Entity entity) {
        //players arent in EntityList, hence...
        String entityId = (entity instanceof EntityPlayer) ? "minecraft:player" : EntityList.getKey(entity).toString();
        return MobFamilyCreator.getAllFamilies().stream()
                .filter(m -> m.getFamilyMembers().contains(entityId))
                .map(MobFamily::getName)
                .collect(Collectors.toList());
    }

    public static List<String> getAllMobFamilyNames() {
        return MobFamilyCreator.getAllFamilies().stream()
                .map(MobFamily::getName)
                .collect(Collectors.toList());
    }

    public static boolean entityInMobFamily(Entity entity, String mobFamilyName) {
        for (MobFamily mobFamily : MobFamilyCreator.getAllFamilies()) {
            if (mobFamily.getName().equals(mobFamilyName)) {
                //players arent in EntityList, hence...
                String entityId = (entity instanceof EntityPlayer) ? "minecraft:player" : EntityList.getKey(entity).toString();
                if (mobFamily.getFamilyMembers().contains(entityId)) return true;
            }
        }
        return false;
    }

    public static MobFamily getMobFamily(String mobFamilyName) {
        for (MobFamily mobFamily : MobFamilyCreator.getAllFamilies()) {
            if (mobFamily.getName().equals(mobFamilyName)) {
                return mobFamily;
            }
        }
        return null;
    }

    //everything for later editing starts here
    public static void addEntityToMobFamily(String entityId, String mobFamilyName) {
        for (MobFamily mobFamily : MobFamilyCreator.getAllFamilies()) {
            if (mobFamily.getName().equals(mobFamilyName)) {
                mobFamily.addToFamilyMembers(entityId);
            }
        }
    }

    public static void removeEntityFromMobFamily(String entityId, String mobFamilyName) {
        for (MobFamily mobFamily : MobFamilyCreator.getAllFamilies()) {
            if (mobFamily.getName().equals(mobFamilyName)) {
                mobFamily.removeFromFamilyMembers(entityId);
            }
        }
    }
}
