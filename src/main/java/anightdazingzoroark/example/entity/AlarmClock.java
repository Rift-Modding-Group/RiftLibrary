package anightdazingzoroark.example.entity;

import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;
import anightdazingzoroark.riftlib.util.MathUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

public class AlarmClock extends EntityLiving implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public AlarmClock(World worldIn) {
        super(worldIn);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "clock", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.alarm_clock.hour_rotation", LoopType.LOOP));
                return PlayState.CONTINUE;
            }
        }));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public List<AnimatableValue> createAnimationVariables() {
        return Arrays.asList(
                new AnimatableValue("hour_rotation", MathUtils.randomInRange(0D, 360D)),
                new AnimatableValue("minute_rotation", MathUtils.randomInRange(0D, 360D))
        );
    }
}
