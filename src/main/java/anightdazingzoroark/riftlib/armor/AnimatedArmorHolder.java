package anightdazingzoroark.riftlib.armor;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataArmor;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Holds animation state for one equipped armor stack.
 */
public abstract class AnimatedArmorHolder implements IAnimatable<AnimationDataArmor> {
    @NotNull
    private final AnimationDataArmor animationData = new AnimationDataArmor(this);
    @NotNull
    private ItemStack stack;

    protected AnimatedArmorHolder(@NotNull ItemStack stack) {
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
    public AnimationDataArmor getAnimationData() {
        return this.animationData;
    }
}