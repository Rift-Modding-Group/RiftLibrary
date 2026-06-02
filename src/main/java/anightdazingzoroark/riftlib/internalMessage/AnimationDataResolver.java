package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;

public class AnimationDataResolver {
    @Nullable
    public static AbstractAnimationData<?, ?> resolveNBTAsData(@NotNull World world, @NotNull NBTTagCompound nbtTagCompound) {
        String targetType = nbtTagCompound.getString("AnimationTargetType");

        //anything commented out will be dealt with later :tm:
        return switch (targetType) {
            case "Entity" -> resolveEntity(world, nbtTagCompound.getInteger("EntityID"));
            case "Projectile" -> resolveEntity(world, nbtTagCompound.getInteger("ProjectileID"));
            case "TileEntity" -> resolveTileEntity(world, nbtTagCompound.getIntArray("TileEntityPos"));
            case "Armor" -> resolveArmor(world, nbtTagCompound);
            //case "ItemStack" -> resolveItemStack(world, nbtTagCompound);
            default -> null;
        };
    }

    private static AbstractAnimationData<?, ?> resolveEntity(@NotNull World world, int entityId) {
        Entity entity = world.getEntityByID(entityId);
        if (!(entity instanceof IAnimatable<?> animatable)) return null;
        return animatable.getAnimationData();
    }

    private static AbstractAnimationData<?, ?> resolveTileEntity(@NotNull World world, int[] posData) {
        if (posData.length != 3) return null;

        TileEntity tileEntity = world.getTileEntity(new BlockPos(posData[0], posData[1], posData[2]));
        if (!(tileEntity instanceof IAnimatable<?> animatable)) return null;
        return animatable.getAnimationData();
    }

    private static AbstractAnimationData<?, ?> resolveArmor(@NotNull World world, @NotNull NBTTagCompound nbtTagCompound) {
        int wearerID = nbtTagCompound.getInteger("WearerID");
        int armorSlotIndex = nbtTagCompound.getInteger("ArmorSlot");
        if (wearerID < 0 || armorSlotIndex < 0 || armorSlotIndex >= EntityEquipmentSlot.values().length) return null;

        Entity wearer = world.getEntityByID(wearerID);
        if (!(wearer instanceof EntityPlayer playerWearer)) return null;

        EntityEquipmentSlot armorSlot = EntityEquipmentSlot.values()[armorSlotIndex];
        ItemStack serverStack = playerWearer.getItemStackFromSlot(armorSlot);
        if (serverStack.isEmpty()) return null;

        ItemStack expectedStack = new ItemStack(nbtTagCompound.getCompoundTag("Stack"));
        if (!isSameStack(serverStack, expectedStack)) return null;

        if (!(serverStack.getItem() instanceof IAnimatable<?> animatable)) return null;
        return animatable.getAnimationData();
    }

    /*
    private static AbstractAnimationData<?, ?> resolveItemStack(@NotNull World world, @NotNull NBTTagCompound nbtTagCompound) {
        int playerHolderID = nbtTagCompound.getInteger("PlayerHolderID");
        int handIndex = nbtTagCompound.getInteger("PlayerHolderHand");
        if (playerHolderID < 0 || handIndex < 0 || handIndex >= EnumHand.values().length) return null;

        Entity holderEntity = world.getEntityByID(playerHolderID);
        if (!(holderEntity instanceof EntityPlayer playerHolder)) return null;

        EnumHand hand = EnumHand.values()[handIndex];
        ItemStack serverStack = playerHolder.getHeldItem(hand);
        if (serverStack.isEmpty()) return null;

        ItemStack expectedStack = new ItemStack(nbtTagCompound.getCompoundTag("Stack"));
        if (!isSameStack(serverStack, expectedStack)) return null;

        return this.createItemStackHolder(world, serverStack.copy());
    }

    private AnimatedItemStackHolder createItemStackHolder(@NotNull World world, ItemStack stack) {
        String holderClassName = this.targetData.getString("HolderClass");
        if (holderClassName.isEmpty()) return null;

        try {
            Class<?> rawHolderClass = Class.forName(holderClassName);
            Class<? extends AnimatedItemStackHolder> holderClass = rawHolderClass.asSubclass(AnimatedItemStackHolder.class);
            Constructor<? extends AnimatedItemStackHolder> constructor = holderClass.getDeclaredConstructor(ItemStack.class);
            constructor.setAccessible(true);
            return constructor.newInstance(stack);
        }
        catch (ReflectiveOperationException | ClassCastException e) {
            return null;
        }
    }
     */

    private static boolean isSameStack(ItemStack first, ItemStack second) {
        return !first.isEmpty()
                && !second.isEmpty()
                && ItemStack.areItemsEqual(first, second)
                && ItemStack.areItemStackTagsEqual(first, second);
    }
}
