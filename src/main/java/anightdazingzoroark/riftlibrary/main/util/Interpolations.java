package anightdazingzoroark.riftlibrary.main.util;

import net.minecraft.util.math.MathHelper;

public class Interpolations {
    public static float lerp(float a, float b, float position) {
        return a + (b - a) * position;
    }

    public static float lerpYaw(float a, float b, float position) {
        a = MathHelper.wrapDegrees(a);
        b = MathHelper.wrapDegrees(b);
        return lerp(a, normalizeYaw(a, b), position);
    }

    public static float normalizeYaw(float a, float b) {
        float diff = a - b;
        if (!(diff > 180.0F) && !(diff < -180.0F)) {
            return b;
        } else {
            diff = Math.copySign(360.0F - Math.abs(diff), diff);
            return a + diff;
        }
    }
}
