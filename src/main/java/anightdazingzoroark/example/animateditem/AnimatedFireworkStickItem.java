package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerNew;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;

import java.util.List;

public class AnimatedFireworkStickItem extends AnimatedItemStackHolder<AnimatedFireworkStickItem> {
    public AnimatedFireworkStickItem(ItemStack stack) {
        super(stack);
    }

    @Override
    public List<AnimationControllerNew<?, AnimationDataItemStack>> createAnimationControllers() {
        return List.of(
                new AnimationControllerNew<AnimatedFireworkStickItem, AnimationDataItemStack>(
                        this, "sparks", "default",
                        new AnimationControllerState<AnimationDataItemStack>("default")
                                .addAnimation("animation.firework_stick.create_sparks")
                )
        );
    }
}
