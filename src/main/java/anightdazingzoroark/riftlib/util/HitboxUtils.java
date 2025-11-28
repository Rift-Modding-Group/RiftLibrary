package anightdazingzoroark.riftlib.util;

public class HitboxUtils {
    public static String locatorHitboxToHitbox(String locatorName) {
        if (!locatorName.startsWith("hitbox_")) return locatorName;
        return locatorName.substring(7);
    }

    public static boolean locatorCanBeHitbox(String locatorName) {
        return locatorName.startsWith("hitbox_");
    }
}
