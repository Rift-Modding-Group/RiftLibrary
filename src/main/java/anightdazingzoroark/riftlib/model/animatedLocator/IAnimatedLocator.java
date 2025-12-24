package anightdazingzoroark.riftlib.model.animatedLocator;

import net.minecraft.util.math.Vec3d;

public interface IAnimatedLocator {
    /***
    * This is for getting the position in the world of the locator in blocks
    ***/
    Vec3d getWorldPosition(float partialTicks);

    /***
     * This is for getting the rotation in the world of the locator in radians
     ***/
    Vec3d getWorldRotation(float partialTicks);

    /***
     * Whether or not the locator is alive depends on if the IAnimatable associated with it
     * is "dead", and "dead" depends on the IAnimatable
     ***/
    boolean isValid();
}
