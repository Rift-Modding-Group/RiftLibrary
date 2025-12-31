package anightdazingzoroark.riftlib.model.animatedLocator;

import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector4f;

public class ItemAnimatedLocator implements IAnimatedLocator {
    private final Item item;
    private final GeoLocator locator;
    private boolean valid;
    private ParticleBuilder particleBuilder;
    //private Quaternion quatView = new Quaternion(0, 0, 0, 1);
    private long lastUpdateFrame = -999L;
    private Vec3d worldPos = Vec3d.ZERO;
    private Vec3d worldRot = Vec3d.ZERO;

    public ItemAnimatedLocator(Item item, GeoLocator locator) {
        this.item = item;
        this.locator = locator;
    }

    @Override
    public Vec3d getWorldPosition() {
        if (!this.wasUpdatedRecently()) this.valid = false;
        return this.worldPos;
    }

    @Override
    public Vec3d getWorldRotation() {
        if (!this.wasUpdatedRecently()) this.valid = false;
        return this.worldRot;
    }

    @Override
    public boolean isValid() {
        if (!this.wasUpdatedRecently()) this.valid = false;
        return this.valid;
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
        return VectorUtils.rotateVector(vector, this.worldRot.x, this.worldRot.y, this.worldRot.z);
    }

    public void updateFromRender(Vec3d worldPos, Vec3d worldRot) {
        this.worldPos = worldPos;
        this.worldRot = worldRot;

        //constantly make validity here true to ensure that until item is discarded
        //it wont exist
        this.valid = true;
        this.lastUpdateFrame = AnimatedLocatorTicker.RENDER_FRAME_ID;
    }

    private boolean wasUpdatedRecently() {
        long frame = AnimatedLocatorTicker.RENDER_FRAME_ID;
        return (this.lastUpdateFrame == frame) || (this.lastUpdateFrame == frame - 1);
    }
}
