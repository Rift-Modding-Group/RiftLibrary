package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class AnimatedFireworkStickItem extends AnimatedItemStackHolder {
    public AnimatedFireworkStickItem(ItemStack stack) {
        super(stack);
    }

    @Override
    public void initializeAnimationData(@NonNull AnimationDataItemStack animationData) {
        animationData.addAnimationController(new AnimationController<AnimatedFireworkStickItem, AnimationDataItemStack>(
                this, "sparks", "default",
                new AnimationControllerState<AnimationDataItemStack>("default")
                        .addAnimation("animation.firework_stick.create_sparks")
        ));
    }
}
