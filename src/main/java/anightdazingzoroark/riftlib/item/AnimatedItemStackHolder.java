package anightdazingzoroark.riftlib.item;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AnimatedItemStackHolder implements IAnimatable<AnimationDataItemStack> {
    @NotNull
    private final AnimationDataItemStack animationData = new AnimationDataItemStack(this);
    @NotNull
    private ItemStack stack;
    @Nullable
    private ItemCameraTransforms.TransformType transformType;

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

    @Nullable
    public ItemCameraTransforms.TransformType getTransformType() {
        return this.transformType;
    }

    public void setTransformType(@Nullable ItemCameraTransforms.TransformType transformType) {
        this.transformType = transformType;
    }

    @Override
    @NotNull
    public AnimationDataItemStack getAnimationData() {
        return this.animationData;
    }
}
