package anightdazingzoroark.riftlib.model.animatedLocator;

import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
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
    private Quaternion quatView = new Quaternion(0, 0, 0, 1);
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
        Quaternion quatLocator = this.getGeoLocator().computeQuaternion();

        //combine quatLocator and quatView
        Quaternion quatCombined = new Quaternion();
        Quaternion.mul(this.quatView, quatLocator, quatCombined);

        Quaternion quatVector = new Quaternion((float) vector.x, (float) vector.y, (float) vector.z, 0f);

        Quaternion quatConj = new Quaternion(quatCombined);
        quatConj.negate(quatConj);

        Quaternion quatFinal = new Quaternion();
        Quaternion.mul(quatCombined, quatVector, quatFinal);
        Quaternion.mul(quatFinal, quatConj, quatFinal);

        return new Vec3d(quatFinal.x, quatFinal.y, quatFinal.z);
    }

    public void updateFromRender(Vec3d worldPos, Vec3d worldRot) {
        this.worldPos = worldPos;
        this.worldRot = worldRot;

        //create quaternions for rotations
        Quaternion quatYaw = this.quatFromAxisAngle(0, 1, 0, (float) worldRot.y);
        Quaternion quatPitch = this.quatFromAxisAngle(1, 0, 0, (float) -worldRot.x);
        Quaternion quatRoll = this.quatFromAxisAngle(0, 0, 1, (float) worldRot.z);

        Quaternion quaternionPoint = new Quaternion();
        Quaternion.mul(quatYaw, quatPitch, quaternionPoint);

        Quaternion quatFinal = new Quaternion();
        Quaternion.mul(quaternionPoint, quatRoll, quatFinal);

        this.quatView = quatFinal;

        //constantly make validity here true to ensure that until item is discarded
        //it wont exist
        this.valid = true;
        this.lastUpdateFrame = AnimatedLocatorTicker.RENDER_FRAME_ID;
    }

    private Quaternion quatFromAxisAngle(float ax, float ay, float az, float angleRad) {
        Vector4f vector = new Vector4f(ax, ay, az, angleRad);
        Quaternion toReturn = new Quaternion();
        toReturn.setFromAxisAngle(vector);
        return toReturn;
    }

    private boolean wasUpdatedRecently() {
        long frame = AnimatedLocatorTicker.RENDER_FRAME_ID;
        return (this.lastUpdateFrame == frame) || (this.lastUpdateFrame == frame - 1);
    }
}
