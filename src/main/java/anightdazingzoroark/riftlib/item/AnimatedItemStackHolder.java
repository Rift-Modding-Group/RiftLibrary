package anightdazingzoroark.riftlib.item;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import net.minecraft.item.ItemStack;

public abstract class AnimatedItemStackHolder<T extends AnimatedItemStackHolder<?>> implements IAnimatable<AnimationDataItemStack> {
    private ItemStack stack;
    private final AnimationDataItemStack animationData = new AnimationDataItemStack(this);

    protected AnimatedItemStackHolder(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public AnimationDataItemStack getAnimationData() {
        return this.animationData;
    }
}
