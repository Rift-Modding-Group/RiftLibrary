package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;

import java.util.List;

public class AnimatedFidgetSpinnerItem extends AnimatedItemStackHolder<AnimatedFidgetSpinnerItem> {
    public AnimatedFidgetSpinnerItem(ItemStack stack) {
        super(stack);
    }

    @Override
    public List<AnimationController<?, AnimationDataItemStack>> createAnimationControllers() {
        return List.of(
                new AnimationController<AnimatedFidgetSpinnerItem, AnimationDataItemStack>(
                        this, "spin", "default",
                        new AnimationControllerState<AnimationDataItemStack>("default")
                                .addAnimation("animation.fidget_spinner.spin")
                )
        );
    }
}
