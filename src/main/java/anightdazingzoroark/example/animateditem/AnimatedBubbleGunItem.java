package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;

public class AnimatedBubbleGunItem extends AnimatedItemStackHolder {
    public AnimatedBubbleGunItem(ItemStack stack) {
        super(stack);
    }

    @Override
    public void registerAnimationControllers(AnimationDataItemStack data) {
        data.addAnimationController(new AnimationController<>(this, "blow", 0, event -> {
            if (data.isBeingUsed()) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bubble_gun.blow_bubbles", LoopType.PLAY_ONCE));
                return PlayState.CONTINUE;
            }

            event.getController().clearAnimationCache();
            return PlayState.STOP;
        }));
    }
}
