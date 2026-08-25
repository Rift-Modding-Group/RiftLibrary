package anightdazingzoroark.example.armor;

import anightdazingzoroark.riftlib.armor.AnimatedArmorHolder;
import anightdazingzoroark.riftlib.core.manager.AnimationDataArmor;
import net.minecraft.item.ItemStack;

public class AnimatedGreenArmorHolder extends AnimatedArmorHolder {
    public AnimatedGreenArmorHolder(ItemStack stack) {
        super(stack);
    }

    @Override
    public void initializeAnimationData(AnimationDataArmor animationData) {}
}