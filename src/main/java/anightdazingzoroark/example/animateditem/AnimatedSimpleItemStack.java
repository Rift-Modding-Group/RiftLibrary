package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.controller.AnimationControllerNew;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;

import java.util.List;

public class AnimatedSimpleItemStack extends AnimatedItemStackHolder<AnimatedSimpleItemStack> {
    public AnimatedSimpleItemStack(ItemStack stack) {
        super(stack);
    }

    @Override
    public List<AnimationControllerNew<?, AnimationDataItemStack>> createAnimationControllers() {
        return List.of();
    }
}
