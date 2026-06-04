package anightdazingzoroark.riftlib.propertySystem.propertyStorage.propertyValue;

import net.minecraft.nbt.NBTTagCompound;

public class StringPropertyValue extends AbstractPropertyValue<String> {
    public StringPropertyValue(String key, String initValue) {
        super(key, initValue);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setString(this.getKey(), this.getValue());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.value = nbtTagCompound.getString(this.getKey());
    }

    @Override
    public Class<String> getHeldClass() {
        return String.class;
    }
}
