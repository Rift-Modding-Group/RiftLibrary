package anightdazingzoroark.riftlib.model;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Quaternion;

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

    public boolean isDead() {
        if (this.animatable instanceof Entity) {
            Entity entity = (Entity) this.animatable;
            return !entity.isEntityAlive();
        }
        //todo: find a way to kill animatedlocators that are attached to tile entities
        else if (this.animatable instanceof TileEntity) {
            TileEntity tileEntity = (TileEntity) this.animatable;
        }
        return false;
    }

    public Vec3d getLocatorWorldPosition() {
        Vec3d locatorPos = this.locator.getPosition();

        if (this.animatable instanceof Entity) {
            Entity entity = (Entity) this.animatable;

            //get locator pos and modify a bit based on entity rotation
            double yawRadians = Math.toRadians(this.getEntityYawRadians());
            double cosYaw = Math.cos(yawRadians);
            double sinYaw = Math.sin(yawRadians);

            double rotatedX = locatorPos.x * cosYaw + locatorPos.z * sinYaw;
            double rotatedZ = locatorPos.x * sinYaw - locatorPos.z * cosYaw;

            return new Vec3d(
                    entity.posX + rotatedX * this.animatable.scale(),
                    entity.posY + this.locator.getPosition().y * this.animatable.scale(),
                    entity.posZ + rotatedZ * this.animatable.scale()
            );
        }
        else if (this.animatable instanceof TileEntity) {
            TileEntity tileEntity = (TileEntity) this.animatable;
            return new Vec3d(
                    tileEntity.getPos().getX() + this.locator.getPosition().x + 0.5D,
                    tileEntity.getPos().getY() + this.locator.getPosition().y,
                    tileEntity.getPos().getZ() - this.locator.getPosition().z + 0.5D
            );
        }
        return Vec3d.ZERO;
    }

    public Vec3d rotateVecByQuaternion(Vec3d vector) {
        Quaternion quaternion = this.locator.computeQuaternion();
        Quaternion vectorQuaternion = new Quaternion((float) vector.x, (float) vector.y, (float) vector.z, 0f);

        Quaternion quatConj = new Quaternion(quaternion);
        quatConj.negate(quatConj);

        Quaternion quatFinal = new Quaternion();
        Quaternion.mul(quaternion, vectorQuaternion, quatFinal);
        Quaternion.mul(quatFinal, quatConj, quatFinal);

        return new Vec3d(quatFinal.x, quatFinal.y, quatFinal.z);
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
}
