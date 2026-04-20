package anightdazingzoroark.example.entity;

import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.util.MathUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

public class AlarmClockEntity extends EntityLiving implements IAnimatable<AnimationDataEntity> {
    private final AnimationDataEntity animationData = new AnimationDataEntity(this);

    public AlarmClockEntity(World worldIn) {
        super(worldIn);
    }

    @Override
    public void registerControllers(AnimationDataEntity data) {
        data.addAnimationController(new AnimationController<>(
                this, "clock", 0,
                event -> {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.alarm_clock.hour_rotation", LoopType.LOOP));
                    return PlayState.CONTINUE;
                }
        ));
    }

    @Override
    public AnimationDataEntity getAnimationData() {
        return this.animationData;
    }

    @Override
    public List<AnimatableValue> createAnimationVariables() {
        return Arrays.asList(
                new AnimatableValue("hour_rotation", MathUtils.randomInRange(0D, 360D)),
                new AnimatableValue("minute_rotation = math.random(0, 360);")
        );
    }

    @Override
    public List<AnimatableValue> tickAnimationVariables() {
        return Arrays.asList(
                new AnimatableValue("minute_rotation = minute_rotation + 1;")
        );
    }
}
