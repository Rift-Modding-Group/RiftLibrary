package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;

public class AnimatedSimpleItemStack extends AnimatedItemStackHolder {
    public AnimatedSimpleItemStack(ItemStack stack) {
        super(stack);
    }

    @Override
    public void registerControllers(AnimationDataItemStack data) {}
}
