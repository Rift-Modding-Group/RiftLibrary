package anightdazingzoroark.riftlib.model.animatedLocator;

import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
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
    public Vec3d getWorldRotation() {
        return null;
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
        Quaternion quaternion = this.getGeoLocator().computeQuaternion();
        Quaternion vectorQuaternion = new Quaternion((float) vector.x, (float) vector.y, (float) vector.z, 0f);

        Quaternion quatConj = new Quaternion(quaternion);
        quatConj.negate(quatConj);

        Quaternion quatFinal = new Quaternion();
        Quaternion.mul(quaternion, vectorQuaternion, quatFinal);
        Quaternion.mul(quatFinal, quatConj, quatFinal);

        return new Vec3d(quatFinal.x, quatFinal.y, quatFinal.z);
    }
}
