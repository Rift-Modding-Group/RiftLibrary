package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;

public class AnimatedFidgetSpinnerItem extends AnimatedItemStackHolder {
    public AnimatedFidgetSpinnerItem(ItemStack stack) {
        super(stack);
    }

    @Override
    public void registerControllers(AnimationDataItemStack data) {
        data.addAnimationController(new AnimationController<>(this, "spin", 0, event -> {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.fidget_spinner.spin", LoopType.LOOP));
            return PlayState.CONTINUE;
        }));
    }
}
