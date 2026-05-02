package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;

import java.util.List;

public class AnimatedBubbleGunItem extends AnimatedItemStackHolder<AnimatedBubbleGunItem> {
    public AnimatedBubbleGunItem(ItemStack stack) {
        super(stack);
    }

    @Override
    public List<AnimationController<?, AnimationDataItemStack>> createAnimationControllers() {
        return List.of(
                new AnimationController<AnimatedBubbleGunItem, AnimationDataItemStack>(
                        this, "blow", "default",
                        new AnimationControllerState<AnimationDataItemStack>("default")
                                .addStateTransition("blow", AnimationDataItemStack::isBeingUsed),
                        new AnimationControllerState<AnimationDataItemStack>("blow")
                                .addAnimation("animation.bubble_gun.blow_bubbles")
                                .addStateTransition("default", data -> !data.isBeingUsed())
                )
        );
    }
}
