package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.armor.RiftLibArmor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public class AnimationDataArmor extends AbstractAnimationData<RiftLibArmor> {
    private EntityLivingBase wearer;
    @NotNull
    private ItemStack stack = ItemStack.EMPTY;
    private EntityEquipmentSlot slot;

    public AnimationDataArmor(RiftLibArmor armor) {
        super(armor, armor);
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


    @Override
    public @NotNull NBTTagCompound asNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        toReturn.setString("AnimationTargetType", "Armor");
        toReturn.setString("ArmorClass", this.getHolder().getClass().getName());
        toReturn.setInteger("WearerID", this.wearer != null ? this.wearer.getEntityId() : -1);
        toReturn.setInteger("ArmorSlot", this.slot != null ? this.slot.ordinal() : -1);
        toReturn.setTag("Stack", this.stack.writeToNBT(new NBTTagCompound()));
        return toReturn;
    }
}
