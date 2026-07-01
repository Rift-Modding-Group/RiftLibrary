package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.util.MiscUtils;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public abstract class AbstractAnimationDataEntity<T extends Entity, D extends AbstractAnimationDataEntity<T, D>> extends AbstractAnimationData<T, D> {
    private double modifiedDistanceMoved;

    public AbstractAnimationDataEntity(@NonNull T holder, @NotNull IAnimatable<D> animatable) {
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

    @Override
    public boolean isValid() {
        return this.getHolder().isEntityAlive();
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

        this.registerMolangQuery("query.modified_distance_moved", (values, animData) -> this.modifiedDistanceMoved);
        this.registerMolangQuery("query.distance_from_camera", (values, animData) -> {
            if (this.getWorld() == null || !this.getWorld().isRemote) return 0D;

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
        this.registerMolangQuery("query.is_on_ground", (values, animData) -> {
            return MolangUtils.booleanToDouble(this.getHolder().onGround);
        });
        this.registerMolangQuery("query.is_in_water", (values, animData) -> {
            return MolangUtils.booleanToDouble(this.getHolder().isInWater());
        });
        this.registerMolangQuery("query.is_in_water_or_rain", (values, animData) -> {
            return MolangUtils.booleanToDouble(this.getHolder().isWet());
        });
        this.registerMolangQuery("query.is_on_fire", (values, animData) -> {
            return MolangUtils.booleanToDouble(this.getHolder().isBurning());
        });
        this.registerMolangQuery("query.ground_speed", (values, animData) -> this.getHorizontalSpeed());
        this.registerMolangQuery("query.vertical_speed", (values, animData) -> this.getVerticalSpeed());
        this.registerMolangQuery("query.yaw_speed", (values, animData) -> {
            if (this.getWorld() == null || !this.getWorld().isRemote) {
                return (double) (this.getHolder().rotationYaw - this.getHolder().prevRotationYaw);
            }
            float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
            float currentEntityYaw = Interpolations.lerpYaw(this.getHolder().prevRotationYaw, this.getHolder().rotationYaw, partialTick);
            float prevEntityYaw = Interpolations.lerpYaw(this.getHolder().prevRotationYaw, this.getHolder().rotationYaw, partialTick - 0.1f);
            return (double) (currentEntityYaw - prevEntityYaw);
        });
        this.registerMolangQuery("query.is_riding", (values, animData) -> {
            return MolangUtils.booleanToDouble(this.getHolder().isRiding());
        });
    }

    @Override
    public World getWorld() {
        return this.getHolder().world;
    }
}
