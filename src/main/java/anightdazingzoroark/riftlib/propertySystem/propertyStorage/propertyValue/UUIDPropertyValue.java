package anightdazingzoroark.riftlib.propertySystem.propertyStorage.propertyValue;

import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public class UUIDPropertyValue extends AbstractPropertyValue<UUID> {
    public UUIDPropertyValue(String key, UUID initValue) {
        super(key, initValue);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setUniqueId(this.getKey(), this.value);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.setValue(nbtTagCompound.getUniqueId(this.getKey()));
    }

    @Override
    public Class<UUID> getHeldClass() {
        return UUID.class;
    }
}
