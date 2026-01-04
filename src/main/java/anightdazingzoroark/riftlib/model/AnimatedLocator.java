package anightdazingzoroark.riftlib.model;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Quaternion;

public class AnimatedLocator {
    private final GeoLocator locator;
    private final IAnimatable iAnimatable;
    private RiftLibParticleEmitter particleEmitter;

    public AnimatedLocator(GeoLocator geoLocator, IAnimatable iAnimatable) {
        this.locator = geoLocator;
        this.iAnimatable = iAnimatable;
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

    public Vec3d getPosition() {
        return this.locator.getPosition();
    }

    public Quaternion getYXZQuaternion() {
        return this.locator.getYXZQuaternion();
    }

    public void createParticleEmitter(ParticleBuilder builder) {
        this.particleEmitter = new RiftLibParticleEmitter(builder, Minecraft.getMinecraft().world, this);
    }

    public RiftLibParticleEmitter getParticleEmitter() {
        return this.particleEmitter;
    }
}
