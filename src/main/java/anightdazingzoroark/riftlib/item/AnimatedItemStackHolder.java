package anightdazingzoroark.riftlib.item;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class AnimatedItemStackHolder implements IAnimatable<AnimationDataItemStack> {
    @NotNull
    private final AnimationDataItemStack animationData = new AnimationDataItemStack(this);
    @NotNull
    private ItemStack stack;

    protected AnimatedItemStackHolder(@NotNull ItemStack stack) {
        this.stack = stack;
    }

    @NotNull
    public ItemStack getStack() {
        return this.stack;
    }

    public void setStack(@NotNull ItemStack stack) {
        this.stack = stack;
    }

    @Override
    @NotNull
    public AnimationDataItemStack getAnimationData() {
        return this.animationData;
    }
}
