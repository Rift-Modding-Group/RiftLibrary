package anightdazingzoroark.riftlib.propertySystem.propertyStorage.propertyValue;

import net.minecraft.nbt.NBTTagCompound;

public class DoublePropertyValue extends AbstractPropertyValue<Double> {
    public DoublePropertyValue(String key, double initValue) {
        super(key, initValue);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setDouble(this.getKey(), this.getValue());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.value = nbtTagCompound.getDouble(this.getKey());
    }

    @Override
    public Class<Double> getHeldClass() {
        return Double.class;
    }
}
