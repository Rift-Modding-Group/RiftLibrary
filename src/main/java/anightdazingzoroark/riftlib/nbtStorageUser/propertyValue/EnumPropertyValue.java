package anightdazingzoroark.riftlib.nbtStorageUser.propertyValue;

import net.minecraft.nbt.NBTTagCompound;

public class EnumPropertyValue<E extends Enum<E>> extends AbstractPropertyValue<E> {
    private final Class<E> enumClass;

    public EnumPropertyValue(String key, Class<E> enumClass, E initValue) {
        super(key, initValue);
        this.enumClass = enumClass;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setInteger(this.getKey(), this.value.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.setValue(this.enumClass.getEnumConstants()[nbtTagCompound.getInteger(this.getKey())]);
    }

    @Override
    public Class<E> getHeldClass() {
        return this.enumClass;
    }
}
