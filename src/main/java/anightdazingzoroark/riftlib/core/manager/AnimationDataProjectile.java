package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import anightdazingzoroark.riftlib.util.MiscUtils;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.function.BiFunction;

public class AnimationDataProjectile extends AbstractAnimationData<RiftLibProjectile> {
    public AnimationDataProjectile(RiftLibProjectile holder) {
        super(holder, getAnimatable(holder));
    }

    @Override
    @NonNull
    public NBTTagCompound asNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        toReturn.setInteger("ProjectileID", this.getHolder().getEntityId());
        return toReturn;
    }

    public boolean isMoving() {
        return this.getHorizontalSpeed() > 0;
    }

    public double getHorizontalSpeed() {
        return MiscUtils.getEntityHorizontalSpeed(this.getHolder());
    }

    public double getVerticalSpeed() {
        return MiscUtils.getEntityVerticalSpeed(this.getHolder());
    }

    @Override
    protected HashMap<String, BiFunction<AbstractAnimationData<RiftLibProjectile>, MolangParser, Double>> getMolangQueries() {
        HashMap<String, BiFunction<AbstractAnimationData<RiftLibProjectile>, MolangParser, Double>> toReturn = super.getMolangQueries();

        toReturn.put("query.modified_distance_moved", (animData, parser) -> {
            double speed = this.getHorizontalSpeed();

            //the reason why modified_distance_moved is calculated as such is because
            //getEntitySpeed returns the speed of an entity within the tick, which is
            //as good as by how many blocks it was displaced within a tick, which is
            //good enough to add upon for this query
            if (speed > 0) {
                double oldValue = parser.getVariable("query.modified_distance_moved").get();
                return oldValue + speed;
            }
            return 0D;
        });
        toReturn.put("query.distance_from_camera", (animData, parser) -> {
            Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
            float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();

            if (camera == null) return 0D;

            Vec3d entityCamera = new Vec3d(
                    Interpolations.lerp(camera.prevPosX, camera.posX, partialTick),
                    Interpolations.lerp(camera.prevPosY, camera.posY, partialTick),
                    Interpolations.lerp(camera.prevPosZ, camera.posZ, partialTick)
            );
            Vec3d entityPosition = new Vec3d(
                    Interpolations.lerp(this.getHolder().prevPosX, this.getHolder().posX, partialTick),
                    Interpolations.lerp(this.getHolder().prevPosY, this.getHolder().posY, partialTick),
                    Interpolations.lerp(this.getHolder().prevPosZ, this.getHolder().posZ, partialTick)
            );
            return entityCamera.add(ActiveRenderInfo.getCameraPosition()).distanceTo(entityPosition);
        });
        toReturn.put("query.is_on_ground", (animData, parser) -> {
            return MolangUtils.booleanToDouble(this.getHolder().onGround);
        });
        toReturn.put("query.is_in_water", (animData, parser) -> {
            return MolangUtils.booleanToDouble(this.getHolder().isInWater());
        });
        toReturn.put("query.is_in_water_or_rain", (animData, parser) -> {
            return MolangUtils.booleanToDouble(this.getHolder().isWet());
        });
        toReturn.put("query.is_on_fire", (animData, parser) -> {
            return MolangUtils.booleanToDouble(this.getHolder().isBurning());
        });
        toReturn.put("query.ground_speed", (animData, parser) -> {
            return this.getHorizontalSpeed();
        });
        toReturn.put("query.vertical_speed", (animData, parser) -> {
            return this.getVerticalSpeed();
        });
        toReturn.put("query.yaw_speed", (animData, parser) -> {
            float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
            float currentEntityYaw = Interpolations.lerpYaw(this.getHolder().prevRotationYaw, this.getHolder().rotationYaw, partialTick);
            float prevEntityYaw = Interpolations.lerpYaw(this.getHolder().prevRotationYaw, this.getHolder().rotationYaw, partialTick - 0.1f);
            return (double) (currentEntityYaw - prevEntityYaw);
        });

        return toReturn;
    }

    private static IAnimatable<?> getAnimatable(RiftLibProjectile holder) {
        if (holder instanceof IAnimatable<?>) return (IAnimatable<?>) holder;
        throw new IllegalArgumentException("AnimationDataProjectile holder must implement IAnimatableNew");
    }
}
