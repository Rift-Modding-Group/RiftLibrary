package anightdazingzoroark.riftlib.nbtStorageUser.propertyValue;

import net.minecraft.nbt.NBTTagCompound;

public class BooleanPropertyValue extends AbstractPropertyValue<Boolean> {
    public BooleanPropertyValue(String key, boolean initValue) {
        super(key, initValue);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setBoolean(this.getKey(), this.value);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.value = nbtTagCompound.getBoolean(this.getKey());
    }

    @Override
    public Class<Boolean> getHeldClass() {
        return Boolean.class;
    }
}
