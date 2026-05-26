package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.util.MiscUtils;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.entity.Entity;
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

        this.molangQueries.put("query.modified_distance_moved", () -> this.modifiedDistanceMoved);
        this.molangQueries.put("query.distance_from_camera", () -> {
            if (!this.getHolder().world.isRemote) return 0D;
            return ClientAnimationDataQueries.distanceFromCamera(this.getHolder());
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
            if (!this.getHolder().world.isRemote) {
                return (double) (this.getHolder().rotationYaw - this.getHolder().prevRotationYaw);
            }
            return ClientAnimationDataQueries.yawSpeed(this.getHolder());
        });
        this.molangQueries.put("query.is_riding", () -> {
            return MolangUtils.booleanToDouble(this.getHolder().isRiding());
        });
    }
}
