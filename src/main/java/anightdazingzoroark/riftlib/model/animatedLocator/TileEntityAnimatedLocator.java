package anightdazingzoroark.riftlib.model.animatedLocator;

import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;

public class TileEntityAnimatedLocator implements IAnimatedLocator {
    private final TileEntity tileEntity;
    private final GeoLocator locator;

    public TileEntityAnimatedLocator(TileEntity tileEntity, GeoLocator locator) {
        this.tileEntity = tileEntity;
        this.locator = locator;
    }

    @Override
    public Vec3d getWorldPosition(float partialTicks) {
        if (this.tileEntity == null || this.tileEntity.isInvalid()) return Vec3d.ZERO;
        return null;
    }

    @Override
    public Vec3d getWorldRotation(float partialTicks) {
        return null;
    }

    @Override
    public boolean isValid() {
        return this.tileEntity != null && !this.tileEntity.isInvalid();
    }
}
