package anightdazingzoroark.riftlib.model.animatedLocator;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

public class EntityAnimatedLocator implements IAnimatedLocator {
    private final Entity entity;
    private final GeoLocator locator;
    private ParticleBuilder particleBuilder;

    public EntityAnimatedLocator(Entity entity, GeoLocator locator) {
        this.entity = entity;
        this.locator = locator;
    }

    @Override
    public Vec3d getWorldPosition() {
        //get locator pos and modify a bit based on entity rotation
        Vec3d locatorPos = this.locator.getPosition();

        double yawRadians = Math.toRadians(this.getEntityYawRadians());
        double cosYaw = Math.cos(yawRadians);
        double sinYaw = Math.sin(yawRadians);

        double rotatedX = locatorPos.x * cosYaw + locatorPos.z * sinYaw;
        double rotatedZ = locatorPos.x * sinYaw - locatorPos.z * cosYaw;

        return new Vec3d(
                this.entity.posX + rotatedX * this.getEntityScale(),
                this.entity.posY + this.locator.getPosition().y * this.getEntityScale(),
                this.entity.posZ + rotatedZ * this.getEntityScale()
        );
    }

    @Override
    public boolean isValid() {
        return this.entity != null && this.entity.isEntityAlive();
    }

    @Override
    public GeoLocator getGeoLocator() {
        return this.locator;
    }

    @Override
    public void setParticleBuilder(ParticleBuilder particleBuilder) {
        this.particleBuilder = particleBuilder;
    }

    @Override
    public ParticleBuilder getParticleBuilder() {
        return this.particleBuilder;
    }

    @Override
    public Vec3d rotateVecByQuaternion(Vec3d vector) {
        return Vec3d.ZERO;
    }

    private double getEntityYawRadians() {
        if (this.entity instanceof EntityLivingBase) {
            EntityLivingBase entityLivingBase = (EntityLivingBase) this.entity;
            return entityLivingBase.renderYawOffset;
        }
        return entity.rotationYaw;
    }

    private float getEntityScale() {
        if (this.entity instanceof IAnimatable) {
            IAnimatable animatable = (IAnimatable) this.entity;
            return animatable.scale();
        }
        return 1f;
    }
}
