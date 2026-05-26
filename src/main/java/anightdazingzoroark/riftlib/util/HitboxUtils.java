package anightdazingzoroark.riftlib.util;

import anightdazingzoroark.riftlib.model.AnimatedLocator;

public class HitboxUtils {
    public static String locatorHitboxToHitbox(String locatorName) {
        if (!locatorName.startsWith("hitbox_")) return locatorName;
        return locatorName.substring(7);
    }

    public static boolean locatorCanBeHitbox(String locatorName) {
        return locatorName.startsWith("hitbox_");
    }

    public static boolean locatorCanBeHitbox(AnimatedLocator locator) {
        return locatorCanBeHitbox(locator.getName());
    }
}
