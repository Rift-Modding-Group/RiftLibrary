package anightdazingzoroark.riftlib.nbtStorageUser.propertyValue;

import net.minecraft.nbt.NBTTagCompound;

public class NullableEnumProperty<E extends Enum<E>> extends AbstractPropertyValue<E> {
    private final Class<E> enumClass;

    public NullableEnumProperty(String key, Class<E> enumClass) {
        this(key, enumClass, null);
    }

    public NullableEnumProperty(String key, Class<E> enumClass, E initValue) {
        super(key, initValue);
        this.enumClass = enumClass;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        int valueToSet = this.value != null ? this.value.ordinal() : -1;
        nbtTagCompound.setInteger(this.getKey(), valueToSet);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        int enumOrdinal = nbtTagCompound.getInteger(this.getKey());
        E valueToSet = enumOrdinal >= 0 ? this.getHeldClass().getEnumConstants()[enumOrdinal] : null;
        this.setValue(valueToSet);
    }

    @Override
    public Class<E> getHeldClass() {
        return this.enumClass;
    }
}
