package anightdazingzoroark.riftlib.item;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AnimatedItemStackHolder implements IAnimatable<AnimationDataItemStack> {
    @NotNull
    private final AnimationDataItemStack animationData = new AnimationDataItemStack(this);
    @NotNull
    private ItemStack stack;
    @Nullable
    private ItemCameraTransforms.TransformType transformType;
    private float firstPersonEquipProgress;

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

    /**
     * @deprecated Use {@link #isHeldByEntity()} to support all entity holders.
     */
    @Deprecated(since = "2.1.0")
    public boolean isHeld() {
        if (this.transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND
                || this.transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
        ) {
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft.player == null) return false;

            EnumHandSide renderedHandSide = this.transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                    ? EnumHandSide.RIGHT : EnumHandSide.LEFT;
            EnumHand renderedHand = minecraft.player.getPrimaryHand() == renderedHandSide ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
            return ItemStack.areItemsEqual(minecraft.player.getHeldItem(renderedHand), this.stack);
        }

        return this.transformType != ItemCameraTransforms.TransformType.GUI && this.animationData.getPlayerHolder() != null;
    }

    /**
     * Entity-aware held-item check
     * */
    public boolean isHeldByEntity() {
        if (this.transformType == ItemCameraTransforms.TransformType.GUI) return false;
        Entity entity = this.animationData.getEntityHolder();
        if (entity instanceof EntityLivingBase) return this.animationData.getEntityHolderHand() != null;
        return entity != null;
    }

    public boolean isFirstPersonEquipAnimationComplete() {
        return this.firstPersonEquipProgress >= 1f;
    }

    public void setFirstPersonEquipProgress(float firstPersonEquipProgress) {
        this.firstPersonEquipProgress = Math.clamp(1f - firstPersonEquipProgress, 0f, 1f);
    }

    @Override
    @NotNull
    public AnimationDataItemStack getAnimationData() {
        return this.animationData;
    }
}
