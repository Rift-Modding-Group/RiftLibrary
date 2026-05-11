package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class RiftLibRunAnimationMessageEffect extends RiftLibMessage<RiftLibRunAnimationMessageEffect> {
    private String effectName;
    private NBTTagCompound targetData;

    public RiftLibRunAnimationMessageEffect() {}

    public RiftLibRunAnimationMessageEffect(String effectName, NBTTagCompound targetData) {
        this.effectName = effectName;
        this.targetData = targetData;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.effectName = ByteBufUtils.readUTF8String(buf);
        this.targetData = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.effectName);
        ByteBufUtils.writeTag(buf, this.targetData);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibRunAnimationMessageEffect message, EntityPlayer player, MessageContext messageContext) {
        if (message.effectName == null || message.targetData == null) return;

        IAnimatable<?> animatable = message.resolveTarget(player);
        if (animatable == null) return;

        Map<String, AnimatableRunValue> effects = animatable.animationMessageEffects();
        Runnable effect = effects.get(message.effectName).runValue();
        if (effect != null) effect.run();
    }

    @Override
    public void executeOnClient(Minecraft client, RiftLibRunAnimationMessageEffect message, EntityPlayer player, MessageContext messageContext) {
        if (message.effectName == null || message.targetData == null) return;

        IAnimatable<?> animatable = message.resolveTarget(player);
        if (animatable == null) return;

        Map<String, AnimatableRunValue> effects = animatable.animationMessageEffects();
        Runnable effect = effects.get(message.effectName).runValue();
        if (effect != null) effect.run();
    }

    //-----note: everything here feels like they could be done in some better way...-----
    private IAnimatable<?> resolveTarget(EntityPlayer sender) {
        String targetType = this.targetData.getString("AnimationTargetType");

        return switch (targetType) {
            case "Entity" -> this.resolveEntity(sender, "EntityID");
            case "Projectile" -> this.resolveEntity(sender, "ProjectileID");
            case "TileEntity" -> this.resolveTileEntity(sender);
            case "Armor" -> this.resolveArmor(sender);
            case "ItemStack" -> this.resolveItemStack(sender);
            default -> null;
        };
    }

    private IAnimatable<?> resolveEntity(EntityPlayer sender, String idKey) {
        Entity entity = sender.world.getEntityByID(this.targetData.getInteger(idKey));
        return entity instanceof IAnimatable<?> animatable ? animatable : null;
    }

    private IAnimatable<?> resolveTileEntity(EntityPlayer sender) {
        int[] posData = this.targetData.getIntArray("TileEntityPos");
        if (posData.length != 3) return null;

        TileEntity tileEntity = sender.world.getTileEntity(new BlockPos(posData[0], posData[1], posData[2]));
        return tileEntity instanceof IAnimatable<?> animatable ? animatable : null;
    }

    private IAnimatable<?> resolveArmor(EntityPlayer sender) {
        int wearerID = this.targetData.getInteger("WearerID");
        int armorSlotIndex = this.targetData.getInteger("ArmorSlot");
        if (wearerID < 0 || armorSlotIndex < 0 || armorSlotIndex >= EntityEquipmentSlot.values().length) return null;

        Entity wearer = sender.world.getEntityByID(wearerID);
        if (!(wearer instanceof EntityPlayer playerWearer)) return null;

        // Only the player wearing the armor may request server effects for it.
        if (playerWearer != sender) return null;

        EntityEquipmentSlot armorSlot = EntityEquipmentSlot.values()[armorSlotIndex];
        ItemStack serverStack = playerWearer.getItemStackFromSlot(armorSlot);
        if (serverStack.isEmpty()) return null;

        ItemStack expectedStack = new ItemStack(this.targetData.getCompoundTag("Stack"));
        if (!this.isSameStack(serverStack, expectedStack)) return null;

        return serverStack.getItem() instanceof IAnimatable<?> animatable ? animatable : null;
    }

    private IAnimatable<?> resolveItemStack(EntityPlayer sender) {
        int playerHolderID = this.targetData.getInteger("PlayerHolderID");
        int handIndex = this.targetData.getInteger("PlayerHolderHand");
        if (playerHolderID < 0 || handIndex < 0 || handIndex >= EnumHand.values().length) return null;

        Entity holderEntity = sender.world.getEntityByID(playerHolderID);
        if (!(holderEntity instanceof EntityPlayer playerHolder)) return null;

        // Only the player holding the stack may request server effects for it.
        if (playerHolder != sender) return null;

        EnumHand hand = EnumHand.values()[handIndex];
        ItemStack serverStack = playerHolder.getHeldItem(hand);
        if (serverStack.isEmpty()) return null;

        ItemStack expectedStack = new ItemStack(this.targetData.getCompoundTag("Stack"));
        if (!this.isSameStack(serverStack, expectedStack)) return null;

        return this.createItemStackHolder(serverStack.copy());
    }

    private AnimatedItemStackHolder createItemStackHolder(ItemStack stack) {
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

    private boolean isSameStack(ItemStack first, ItemStack second) {
        return !first.isEmpty()
                && !second.isEmpty()
                && ItemStack.areItemsEqual(first, second)
                && ItemStack.areItemStackTagsEqual(first, second);
    }
}
