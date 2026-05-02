package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.controller.AnimationControllerNew;
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
    public List<AnimationControllerNew<?, AnimationDataItemStack>> createAnimationControllers() {
        return List.of(
                new AnimationControllerNew<AnimatedFidgetSpinnerItem, AnimationDataItemStack>(
                        this, "spin", "default",
                        new AnimationControllerState<AnimationDataItemStack>("default")
                                .addAnimation("animation.fidget_spinner.spin")
                )
        );
    }
}
