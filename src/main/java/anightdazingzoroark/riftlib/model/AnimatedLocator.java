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

public class AnimatedLocator {
    private final GeoLocator locator;
    private final AbstractAnimationData<?> animationData;
    private final List<RiftLibParticleEmitter> particleEmitterList = new ArrayList<>();
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
        if (this.animationData instanceof AnimationDataEntity entityData) {
            return entityData.getHolder().isEntityAlive();
        }
        else if (this.animationData instanceof AnimationDataTileEntity tileEntityData) {
            return !tileEntityData.getHolder().isInvalid();
        }
        return true;
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
        this.particleEmitterList.add(emitterToAdd);
        ParticleTicker.EMITTER_LIST.add(emitterToAdd);
    }

    public void removeParticleEmitter(RiftLibParticleEmitter emitter) {
        this.particleEmitterList.remove(emitter);
    }

    public List<RiftLibParticleEmitter> getParticleEmitterList() {
        return this.particleEmitterList;
    }
}
