package anightdazingzoroark.riftlib.model;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.render.GeoBone;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleTicker;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Quaternion;

import java.util.ArrayList;
import java.util.List;

public class AnimatedLocator {
    private final GeoLocator locator;
    private final IAnimatable iAnimatable;
    private final List<RiftLibParticleEmitter> particleEmitterList = new ArrayList<>();
    private Vec3d worldSpacePos = new Vec3d(0, 0, 0);
    private Quaternion worldSpaceYXZQuaternion = new Quaternion();
    private boolean isUpdated = true;

    public AnimatedLocator(GeoLocator geoLocator, IAnimatable iAnimatable) {
        this.locator = geoLocator;
        this.iAnimatable = iAnimatable;
    }

    public void setUpdated(boolean value) {
        this.isUpdated = value;
    }

    public boolean isUpdated() {
        return this.isUpdated;
    }

    public boolean isValid() {
        if (this.iAnimatable instanceof Entity) {
            Entity entityAnimatable = (Entity) this.iAnimatable;
            return entityAnimatable.isEntityAlive();
        }
        else if (this.iAnimatable instanceof TileEntity) {
            TileEntity tileEntityAnimatable = (TileEntity) this.iAnimatable;
            return !tileEntityAnimatable.isInvalid();
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
