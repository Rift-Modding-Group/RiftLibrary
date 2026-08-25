package anightdazingzoroark.example.armor;

import anightdazingzoroark.riftlib.armor.AnimatedArmorHolder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataArmor;
import net.minecraft.item.ItemStack;

public class AnimatedSatelliteDishHelmetHolder extends AnimatedArmorHolder {
    public AnimatedSatelliteDishHelmetHolder(ItemStack stack) {
        super(stack);
    }

    @Override
    public void initializeAnimationData(AnimationDataArmor animationData) {
        animationData.addAnimationController(new AnimationController<AnimatedSatelliteDishHelmetHolder, AnimationDataArmor>(
                this, "satelliteDish", "default",
                new AnimationControllerState<AnimationDataArmor>("default")
                        .addAnimation("animation.satellite_dish_helmet.spin")
                        .addAnimation("animation.satellite_dish_helmet.signal")
        ));
    }
}
