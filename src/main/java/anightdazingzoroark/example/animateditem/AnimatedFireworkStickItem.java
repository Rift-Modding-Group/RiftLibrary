package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;

public class AnimatedFireworkStickItem extends AnimatedItemStackHolder {
    public AnimatedFireworkStickItem(ItemStack stack) {
        super(stack);
    }

    @Override
    public void registerControllers(AnimationDataItemStack data) {
        data.addAnimationController(new AnimationController<>(this, "sparks", 0, event -> {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.firework_stick.create_sparks", LoopType.PLAY_ONCE));
            return PlayState.CONTINUE;
        }));
    }
}
