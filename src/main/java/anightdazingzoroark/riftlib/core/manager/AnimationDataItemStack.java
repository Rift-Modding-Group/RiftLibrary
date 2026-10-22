package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnimationDataItemStack extends AbstractAnimationData<AnimatedItemStackHolder, AnimationDataItemStack> {
    @Nullable
    private Entity entityHolder;
    @Nullable
    private EnumHand entityHolderHand;
    private boolean hasRenderContext;

    public AnimationDataItemStack(AnimatedItemStackHolder holder) {
        super(holder, holder);
    }

    @Override
    public void updateOnDataTick() {}

    @Override
    public boolean isValid() {
        return true;
    }

    /**
     * Associates this animation with the entity actually being rendered, rather than an equal stack elsewhere.
     * */
    public void setRenderContext(@Nullable Entity entity, @Nullable EnumHand hand) {
        this.entityHolder = entity;
        this.entityHolderHand = hand;
        this.hasRenderContext = true;
    }

    /**
     * Identifies the rendered item and its entity holder for animation messages.
     * EntityHolderHand is a byte ordinal, or -1 when no hand applies.
     */
    @Override
    @NotNull
    public NBTTagCompound getNBT() {
        NBTTagCompound toReturn = super.getNBT();
        ItemStack stack = this.getHolder().getStack();
        toReturn.setString("AnimationTargetType", "ItemStack");
        toReturn.setString("HolderClass", this.getHolder().getClass().getName());
        toReturn.setTag("Stack", stack.writeToNBT(new NBTTagCompound()));

        Entity holder = this.getEntityHolder();
        EnumHand hand = this.getEntityHolderHand();
        toReturn.setInteger("EntityHolderID", holder != null ? holder.getEntityId() : -1);
        toReturn.setByte("EntityHolderHand", (byte) (hand != null ? hand.ordinal() : -1));
        return toReturn;
    }

    @Nullable
    public Entity getEntityHolder() {
        ItemStack stack = this.getHolder().getStack();
        if (stack.isEmpty()) return null;
        if (this.hasRenderContext) {
            return this.entityHolder != null && !this.entityHolder.isDead ? this.entityHolder : null;
        }

        // Holders created outside the renderer can resolve from the loaded entities.
        World world = this.getWorld();
        if (world == null) return null;
        for (Entity entity : world.loadedEntityList) {
            if (entity instanceof EntityLivingBase living && (this.isSameStack(living.getHeldItemMainhand(), stack)
                    || this.isSameStack(living.getHeldItemOffhand(), stack))
            ) {
                return living;
            }
        }
        return null;
    }

    @Nullable
    public EnumHand getEntityHolderHand() {
        Entity entity = this.getEntityHolder();
        if (!(entity instanceof EntityLivingBase living)) return null;
        ItemStack stack = this.getHolder().getStack();
        if (this.hasRenderContext) {
            if (this.entityHolderHand != null && this.isSameStack(living.getHeldItem(this.entityHolderHand), stack)) {
                return this.entityHolderHand;
            }
            return null;
        }
        if (this.isSameStack(living.getHeldItemMainhand(), stack)) return EnumHand.MAIN_HAND;
        if (this.isSameStack(living.getHeldItemOffhand(), stack)) return EnumHand.OFF_HAND;
        return null;
    }

    /**
     * Entity-aware counterpart to the legacy player-only isBeingUsed() check.
     * */
    public boolean isBeingUsedByEntity() {
        Entity entity = this.getEntityHolder();
        return entity instanceof EntityLivingBase living && living.isHandActive()
                && this.getEntityHolderHand() == living.getActiveHand()
                && this.isSameStack(living.getActiveItemStack(), this.getHolder().getStack());
    }

    /**
     * @deprecated Use {@link #getEntityHolder()} to support all entity holders.
     */
    @Deprecated(since = "2.1.0")
    @Nullable
    public EntityPlayer getPlayerHolder() {
        Entity entity = this.getEntityHolder();
        return entity instanceof EntityPlayer player ? player : null;
    }

    /**
     * @deprecated Use {@link #getEntityHolderHand()} to support all living entity holders.
     */
    @Deprecated(since = "2.1.0")
    @Nullable
    public EnumHand getPlayerHolderHand() {
        return this.getEntityHolderHand();
    }

    /**
     * @deprecated Use {@link #isBeingUsedByEntity()} to support all living entity holders.
     */
    @Deprecated(since = "2.1.0")
    public boolean isBeingUsed() {
        return this.isBeingUsedByEntity();
    }

    private boolean isSameStack(ItemStack first, ItemStack second) {
        return !first.isEmpty()
                && !second.isEmpty()
                && ItemStack.areItemsEqual(first, second)
                && ItemStack.areItemStackTagsEqual(first, second);
    }

    @Override
    public World getWorld() {
        return Minecraft.getMinecraft().world; //temporary as AnimatedItemStackHolder instances are client only... for now
    }
}
