package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;

public class AnimatedBubbleGunItem extends AnimatedItemStackHolder<AnimatedBubbleGunItem> {
    public AnimatedBubbleGunItem(ItemStack stack) {
        super(stack);
    }

    @Override
    public void initializeAnimationData(AnimationDataItemStack animationData) {
        animationData.addAnimationController(new AnimationController<AnimatedBubbleGunItem, AnimationDataItemStack>(
                this, "blow", "default",
                new AnimationControllerState<AnimationDataItemStack>("default")
                        .addStateTransition("blow", AnimationDataItemStack::isBeingUsed),
                new AnimationControllerState<AnimationDataItemStack>("blow")
                        .addAnimation("animation.bubble_gun.blow_bubbles")
                        .addStateTransition("default", data -> !data.isBeingUsed())
        ));
    }
}
