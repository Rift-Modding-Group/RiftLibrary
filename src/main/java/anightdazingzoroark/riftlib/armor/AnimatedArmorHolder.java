package anightdazingzoroark.riftlib.armor;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataArmor;
import net.minecraft.item.ItemStack;

/**
 * Holds animation state for one equipped armor stack.
 */
public abstract class AnimatedArmorHolder implements IAnimatable<AnimationDataArmor> {
    private ItemStack stack;
    private final AnimationDataArmor animationData = new AnimationDataArmor(this);

    protected AnimatedArmorHolder(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public AnimationDataArmor getAnimationData() {
        return this.animationData;
    }
}