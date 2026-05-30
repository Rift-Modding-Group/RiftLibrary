package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;

public class AnimatedFidgetSpinnerItem extends AnimatedItemStackHolder<AnimatedFidgetSpinnerItem> {
    public AnimatedFidgetSpinnerItem(ItemStack stack) {
        super(stack);
    }

    @Override
    public void initializeAnimationData(AnimationDataItemStack animationData) {}
}
