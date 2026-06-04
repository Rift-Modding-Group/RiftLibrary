package anightdazingzoroark.riftlib.propertySystem.propertyStorage.propertyValue;

import net.minecraft.nbt.NBTTagCompound;

public class IntegerPropertyValue extends AbstractPropertyValue<Integer> {
    public IntegerPropertyValue(String key, Integer defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setInteger(this.getKey(), this.getValue());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.value = nbtTagCompound.getInteger(this.getKey());
    }

    @Override
    public Class<Integer> getHeldClass() {
        return Integer.class;
    }
}
