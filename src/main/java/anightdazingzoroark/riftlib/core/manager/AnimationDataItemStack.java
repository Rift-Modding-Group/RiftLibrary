package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class AnimationDataItemStack extends AbstractAnimationData<AnimatedItemStackHolder> {
    public AnimationDataItemStack(AnimatedItemStackHolder holder) {
        super(holder, holder);
    }

    @Override
    public @NotNull NBTTagCompound asNBT() {
        return new NBTTagCompound();
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
}
