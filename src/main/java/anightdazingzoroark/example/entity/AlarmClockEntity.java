package anightdazingzoroark.example.entity;

import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.util.MathUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

import java.util.List;

public class AlarmClockEntity extends EntityLiving implements IAnimatable<AnimationDataEntity> {
    private final AnimationDataEntity animationData = new AnimationDataEntity(this);

    public AlarmClockEntity(World worldIn) {
        super(worldIn);
    }

    @Override
    public List<AnimationController<?, AnimationDataEntity>> createAnimationControllers() {
        return List.of(
                new AnimationController<AlarmClockEntity, AnimationDataEntity>(
                        this, "clock", "default",
                        new AnimationControllerState<AnimationDataEntity>("default")
                                .addAnimation("animation.alarm_clock.hour_rotation")
                )
        );
    }

    @Override
    public AnimationDataEntity getAnimationData() {
        return this.animationData;
    }

    @Override
    public List<AnimatableValue> createAnimationVariables() {
        return List.of(
                new AnimatableValue("hour_rotation", MathUtils.randomInRange(0D, 360D)),
                new AnimatableValue("minute_rotation = math.random(0, 360);")
        );
    }

    @Override
    public List<AnimatableValue> tickAnimationVariables() {
        return List.of(
                new AnimatableValue("minute_rotation = minute_rotation + 1;")
        );
    }
}
