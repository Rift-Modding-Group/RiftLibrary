package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.util.MiscUtils;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public abstract class AbstractAnimationDataEntity<T extends Entity> extends AbstractAnimationData<T> {
    private double modifiedDistanceMoved;

    public AbstractAnimationDataEntity(@NonNull T holder, @NotNull IAnimatable<?> animatable) {
        super(holder, animatable);
    }

    @Override
    public void updateOnDataTick() {
        double speed = this.getHorizontalSpeed();

        //the reason why modified_distance_moved is calculated as such is because
        //getEntitySpeed returns the speed of an entity within the tick, which is
        //as good as by how many blocks it was displaced within a tick, which is
        //good enough to add upon for this query
        if (speed > 0) this.modifiedDistanceMoved += speed;
        else this.modifiedDistanceMoved = 0;
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
    protected void createMolangQueries() {
        super.createMolangQueries();

        this.molangQueries.put("query.modified_distance_moved", () -> this.modifiedDistanceMoved);
        this.molangQueries.put("query.distance_from_camera", () -> {
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
        this.molangQueries.put("query.is_on_ground", () -> {
            return MolangUtils.booleanToDouble(this.getHolder().onGround);
        });
        this.molangQueries.put("query.is_in_water", () -> {
            return MolangUtils.booleanToDouble(this.getHolder().isInWater());
        });
        this.molangQueries.put("query.is_in_water_or_rain", () -> {
            return MolangUtils.booleanToDouble(this.getHolder().isWet());
        });
        this.molangQueries.put("query.is_on_fire", () -> {
            return MolangUtils.booleanToDouble(this.getHolder().isBurning());
        });
        this.molangQueries.put("query.ground_speed", this::getHorizontalSpeed);
        this.molangQueries.put("query.vertical_speed", this::getVerticalSpeed);
        this.molangQueries.put("query.yaw_speed", () -> {
            float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
            float currentEntityYaw = Interpolations.lerpYaw(this.getHolder().prevRotationYaw, this.getHolder().rotationYaw, partialTick);
            float prevEntityYaw = Interpolations.lerpYaw(this.getHolder().prevRotationYaw, this.getHolder().rotationYaw, partialTick - 0.1f);
            return (double) (currentEntityYaw - prevEntityYaw);
        });
        this.molangQueries.put("query.is_riding", () -> {
            return MolangUtils.booleanToDouble(this.getHolder().isRiding());
        });
    }
}
