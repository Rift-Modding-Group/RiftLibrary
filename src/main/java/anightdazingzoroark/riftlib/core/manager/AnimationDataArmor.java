package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.armor.AnimatedArmorHolder;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class AnimationDataArmor extends AbstractAnimationData<AnimatedArmorHolder, AnimationDataArmor> {
    private EntityLivingBase wearer;
    @NotNull
    private ItemStack stack = ItemStack.EMPTY;
    private EntityEquipmentSlot slot;

    public AnimationDataArmor(AnimatedArmorHolder holder) {
        super(holder, holder);
    }

    @Override
    public void updateOnDataTick() {}

    @Override
    public boolean isValid() {
        return this.isEquipped();
    }

    public void setRenderContext(EntityLivingBase wearer, ItemStack stack, EntityEquipmentSlot slot) {
        this.wearer = wearer;
        this.stack = stack;
        this.slot = slot;
    }

    public EntityLivingBase getWearer() {
        return this.wearer;
    }

    @NotNull
    public ItemStack getStack() {
        return this.stack;
    }

    public EntityEquipmentSlot getSlot() {
        return this.slot;
    }

    public boolean isEquipped() {
        if (this.wearer == null || this.slot == null || this.stack.isEmpty()) return false;

        ItemStack equipped = this.wearer.getItemStackFromSlot(this.slot);
        return !equipped.isEmpty()
                && ItemStack.areItemsEqual(equipped, this.stack)
                && ItemStack.areItemStackTagsEqual(equipped, this.stack);
    }

    public int getCurrentDurability() {
        return this.stack.getMaxDamage() - this.stack.getItemDamage();
    }

    public int getMaxDurability() {
        return this.stack.getMaxDamage();
    }

    @Override
    public @NotNull NBTTagCompound getNBT() {
        NBTTagCompound toReturn = super.getNBT();
        toReturn.setString("AnimationTargetType", "Armor");
        toReturn.setString("HolderClass", this.getHolder().getClass().getName());
        toReturn.setString("ArmorClass", this.stack.getItem().getClass().getName());
        toReturn.setInteger("WearerID", this.wearer != null ? this.wearer.getEntityId() : -1);
        toReturn.setInteger("ArmorSlot", this.slot != null ? this.slot.ordinal() : -1);
        toReturn.setTag("Stack", this.stack.writeToNBT(new NBTTagCompound()));
        return toReturn;
    }

    @Override
    public World getWorld() {
        if (this.wearer == null) return null;
        return this.wearer.world;
    }
}
