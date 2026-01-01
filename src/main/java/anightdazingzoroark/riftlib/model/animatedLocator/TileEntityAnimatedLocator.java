package anightdazingzoroark.riftlib.model.animatedLocator;

import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Quaternion;

public class TileEntityAnimatedLocator implements IAnimatedLocator {
    private final TileEntity tileEntity;
    private final GeoLocator locator;
    private ParticleBuilder particleBuilder;

    public TileEntityAnimatedLocator(TileEntity tileEntity, GeoLocator locator) {
        this.tileEntity = tileEntity;
        this.locator = locator;
    }

    @Override
    public Vec3d getWorldPosition() {
        if (this.tileEntity == null || this.tileEntity.isInvalid()) return Vec3d.ZERO;
        return new Vec3d(
                this.tileEntity.getPos().getX() + this.locator.getPosition().x + 0.5D,
                this.tileEntity.getPos().getY() + this.locator.getPosition().y,
                this.tileEntity.getPos().getZ() - this.locator.getPosition().z + 0.5D
        );
    }

    @Override
    public boolean isValid() {
        return this.tileEntity != null && !this.tileEntity.isInvalid();
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
        Quaternion quatLocator = this.getGeoLocator().getXYZQuaternion();

        //get inverse of quatLocator
        Quaternion quatLocatorConj = new Quaternion();
        Quaternion.negate(quatLocator, quatLocatorConj);

        //get quat of vector
        Quaternion quatVector = new Quaternion((float) vector.x, (float) vector.y, (float) vector.z, 0);

        //multiply and get final result
        Quaternion temp = new Quaternion();
        Quaternion result = new Quaternion();
        Quaternion.mul(quatLocator, quatVector, temp);
        Quaternion.mul(temp, quatLocatorConj, result);

        return new Vec3d(result.x, result.y, result.z);
    }
}
