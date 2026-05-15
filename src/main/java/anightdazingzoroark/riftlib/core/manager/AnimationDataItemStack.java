package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class AnimationDataItemStack extends AbstractAnimationData<AnimatedItemStackHolder> {
    public AnimationDataItemStack(AnimatedItemStackHolder holder) {
        super(holder, holder);
    }

    @Override
    public void updateOnDataTick() {}

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public @NotNull NBTTagCompound asNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        ItemStack stack = this.getHolder().getStack();
        EntityPlayer playerHolder = this.getPlayerHolder();
        EnumHand playerHolderHand = playerHolder == null ? null : this.getPlayerHoldingHand(playerHolder, stack);

        toReturn.setString("AnimationTargetType", "ItemStack");
        toReturn.setString("HolderClass", this.getHolder().getClass().getName());
        toReturn.setTag("Stack", stack.writeToNBT(new NBTTagCompound()));

        int playerHolderID = playerHolder != null ? playerHolder.getEntityId() : -1;
        toReturn.setInteger("PlayerHolderID", playerHolderID);
        toReturn.setInteger("PlayerHolderHand", playerHolderHand != null ? playerHolderHand.ordinal() : -1);
        return toReturn;
    }

    //get the player holding the itemstack. null if its not held
    public EntityPlayer getPlayerHolder() {
        ItemStack stack = this.getHolder().getStack();
        if (stack.isEmpty()) return null;

        World world = Minecraft.getMinecraft().world;
        if (world == null) return null;

        for (EntityPlayer player : world.playerEntities) {
            if (this.isSameStack(player.getHeldItemMainhand(), stack) || this.isSameStack(player.getHeldItemOffhand(), stack)) {
                return player;
            }
        }

        return null;
    }

    public EnumHand getPlayerHolderHand() {
        ItemStack stack = this.getHolder().getStack();
        if (stack.isEmpty()) return null;

        EntityPlayer playerHolder = this.getPlayerHolder();
        return playerHolder == null ? null : this.getPlayerHoldingHand(playerHolder, stack);
    }

    //check if an item is being right click held on
    public boolean isBeingUsed() {
        ItemStack stack = this.getHolder().getStack();
        if (stack.isEmpty()) return false;

        World world = Minecraft.getMinecraft().world;
        if (world == null) return false;

        for (EntityPlayer player : world.playerEntities) {
            if (player.isHandActive() && this.isSameStack(player.getActiveItemStack(), stack)) {
                return true;
            }
        }

        return false;
    }

    private boolean isSameStack(ItemStack first, ItemStack second) {
        return !first.isEmpty()
                && !second.isEmpty()
                && ItemStack.areItemsEqual(first, second)
                && ItemStack.areItemStackTagsEqual(first, second);
    }

    private EnumHand getPlayerHoldingHand(EntityPlayer player, ItemStack stack) {
        if (this.isSameStack(player.getHeldItemMainhand(), stack)) return EnumHand.MAIN_HAND;
        if (this.isSameStack(player.getHeldItemOffhand(), stack)) return EnumHand.OFF_HAND;
        return null;
    }
}
