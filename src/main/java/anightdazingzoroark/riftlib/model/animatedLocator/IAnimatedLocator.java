package anightdazingzoroark.riftlib.model.animatedLocator;

import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import net.minecraft.util.math.Vec3d;

public interface IAnimatedLocator {
    /***
    * This is for getting the position in the world of the locator in blocks
    ***/
    Vec3d getWorldPosition();

    /***
     * This is for getting the rotation in the world of the locator in radians
     ***/
    Vec3d getWorldRotation();

    /***
     * Whether or not the locator is alive depends on if the IAnimatable associated with it
     * is "dead", and "dead" depends on the IAnimatable
     ***/
    boolean isValid();

    GeoLocator getGeoLocator();

    /***
     * AnimatedLocators may have particles dynamically spewing out of them based on the
     * given animations. This method will be used to set it so that it can be then created
     * in AnimatedLocatorTicker
     ***/
    void setParticleBuilder(ParticleBuilder particleBuilder);

    ParticleBuilder getParticleBuilder();

    /***
     * This is for vectors signifying the direction in which to emit particles to.
     * Its purpose is to rotate it based on a variety of factors
     * ***/
    Vec3d rotateVecByQuaternion(Vec3d vector);

    default String getLocatorName() {
        return this.getGeoLocator().name;
    }
}
