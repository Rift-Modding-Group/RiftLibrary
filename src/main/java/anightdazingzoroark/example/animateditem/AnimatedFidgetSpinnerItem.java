package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class AnimatedFidgetSpinnerItem extends AnimatedItemStackHolder {
    public AnimatedFidgetSpinnerItem(ItemStack stack) {
        super(stack);
    }

    @Override
    public void initializeAnimationData(@NonNull AnimationDataItemStack animationData) {
        animationData.addAnimationController(new AnimationController<AnimatedFidgetSpinnerItem, AnimationDataItemStack>(
                this, "spin", "default",
                new AnimationControllerState<AnimationDataItemStack>("default")
                        .addAnimation("animation.fidget_spinner.spin")
        ));
    }
}
