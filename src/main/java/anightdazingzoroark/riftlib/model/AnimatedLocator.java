package anightdazingzoroark.riftlib.model;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

//this is for obtaining the position of a locator from an IAnimatable
public class AnimatedLocator {
    private final IAnimatable animatable;
    private final GeoLocator locator;

    public AnimatedLocator(IAnimatable animatable, GeoLocator locator) {
        this.animatable = animatable;
        this.locator = locator;
    }

    public String getLocatorName() {
        return this.locator.name;
    }

    public Vec3d getLocatorWorldPosition() {
        Vec3d animatableWorldPos = this.getAnimatableWorldPosition();
        Vec3d locatorPos = this.locator.getPosition();

        if (this.animatable instanceof Entity) {
            //get locator pos and modify a bit based on entity rotation
            double yawRadians = Math.toRadians(this.getEntityYawRadians());
            double cosYaw = Math.cos(yawRadians);
            double sinYaw = Math.sin(yawRadians);

            double rotatedX = locatorPos.x * cosYaw + locatorPos.z * sinYaw;
            double rotatedZ = locatorPos.x * sinYaw - locatorPos.z * cosYaw;

            return new Vec3d(
                    animatableWorldPos.x + rotatedX * this.animatable.scale(),
                    animatableWorldPos.y + this.locator.getPosition().y * this.animatable.scale(),
                    animatableWorldPos.z + rotatedZ * this.animatable.scale()
            );
        }
        return animatableWorldPos;
    }

    private double getEntityYawRadians() {
        if (this.animatable instanceof EntityLivingBase) {
            EntityLivingBase entityLivingBase = (EntityLivingBase) this.animatable;
            return entityLivingBase.renderYawOffset;
        }
        else if (this.animatable instanceof Entity) {
            Entity entity = (Entity) this.animatable;
            return entity.rotationYaw;
        }
        return 0;
    }

    private Vec3d getAnimatableWorldPosition() {
        //get animatable as entity
        if (this.animatable instanceof Entity) {
            Entity entity = (Entity) this.animatable;
            return new Vec3d(
                    entity.posX,
                    entity.posY,
                    entity.posZ
            );
        }
        return Vec3d.ZERO;
    }
}
