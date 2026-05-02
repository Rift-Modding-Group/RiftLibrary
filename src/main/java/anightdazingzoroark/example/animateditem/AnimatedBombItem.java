package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;

import java.util.List;

public class AnimatedBombItem extends AnimatedItemStackHolder<AnimatedBombItem> {
    public AnimatedBombItem(ItemStack stack) {
        super(stack);
    }

    @Override
    public List<AnimationController<?, AnimationDataItemStack>> createAnimationControllers() {
        return List.of();
    }
}
