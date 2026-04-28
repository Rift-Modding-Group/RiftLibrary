package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;

public class AnimatedBombItem extends AnimatedItemStackHolder {
    public AnimatedBombItem(ItemStack stack) {
        super(stack);
    }

    @Override
    public void registerAnimationControllers(AnimationDataItemStack data) {}
}
