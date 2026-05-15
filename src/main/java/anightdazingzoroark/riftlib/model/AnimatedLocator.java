package anightdazingzoroark.riftlib.model;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.core.manager.AnimationDataTileEntity;
import anightdazingzoroark.riftlib.geo.render.GeoBone;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleTicker;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import org.lwjglx.util.vector.Quaternion;

import java.util.ArrayList;
import java.util.List;

/**
 * AnimatedLocator is for client use only and is built to be for transforming
 * GeoLocators from model space to world space. Its only for holding particles
 * because attempting to use GeoLocator only to do so has resulted unfeasible.
 * (dont ask how i found that out the hard way)
 * <br /><br />
 * Todo: maybe deprecate this once more once I figure out how to finally
 * calculate precisely locator positions on items and armor in-world
 * in a server-friendly way.
 * */
public class AnimatedLocator {
    private final GeoLocator locator;
    private final AbstractAnimationData<?> animationData;
    private Vec3d worldSpacePos = new Vec3d(0, 0, 0);
    private Quaternion worldSpaceYXZQuaternion = new Quaternion();
    private boolean isUpdated = true;

    public AnimatedLocator(GeoLocator geoLocator, AbstractAnimationData<?> animationData) {
        this.locator = geoLocator;
        this.animationData = animationData;
    }

    public void setUpdated(boolean value) {
        this.isUpdated = value;
    }

    public boolean isUpdated() {
        return this.isUpdated;
    }

    public boolean isValid() {
        return this.animationData.isValid();
    }

    public String getName() {
        return this.locator.name;
    }

    public GeoBone getParentBone() {
        return this.locator.parent;
    }

    public void setWorldSpacePosition(double x, double y, double z) {
        this.worldSpacePos = new Vec3d(x, y, z);
    }

    public Vec3d getWorldSpacePosition() {
        return this.worldSpacePos;
    }

    public Vec3d getModelSpacePosition() {
        return this.locator.getPosition();
    }

    public void setWorldSpaceYXZQuaternion(Quaternion quaternion) {
        this.worldSpaceYXZQuaternion = quaternion;
    }

    public Quaternion getWorldSpaceYXZQuaternion() {
        return this.worldSpaceYXZQuaternion;
    }

    public Quaternion getModelSpaceYXZQuaternion() {
        return this.locator.getYXZQuaternion();
    }

    public void createParticleEmitter(ParticleBuilder builder) {
        RiftLibParticleEmitter emitterToAdd = new RiftLibParticleEmitter(builder, Minecraft.getMinecraft().world, this);
        ParticleTicker.EMITTER_LIST.add(emitterToAdd);
    }
}
