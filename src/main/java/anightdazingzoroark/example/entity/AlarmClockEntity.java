package anightdazingzoroark.example.entity;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.util.MathUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class AlarmClockEntity extends EntityLiving implements IAnimatable<AnimationDataEntity> {
    private final AnimationDataEntity animationData = new AnimationDataEntity(this);

    public AlarmClockEntity(World worldIn) {
        super(worldIn);
    }

    @Override
    public void initializeAnimationData(AnimationDataEntity animationData) {
        animationData.addAnimationController(new AnimationController<AlarmClockEntity, AnimationDataEntity>(
                this, "clock", "default",
                new AnimationControllerState<AnimationDataEntity>("default")
                        .addAnimation("animation.alarm_clock.hour_rotation")
        ));

        animationData.addInitAnimationValue("hour_rotation", MathUtils.randomInRange(0D, 360D));
        animationData.addInitAnimationValue("minute_rotation", MathUtils.randomInRange(0D, 360D));

        animationData.addOnUpdateAnimationValue("minute_rotation", () -> animationData.getVariable("minute_rotation") + 1);
    }

    @Override
    public AnimationDataEntity getAnimationData() {
        return this.animationData;
    }
}
