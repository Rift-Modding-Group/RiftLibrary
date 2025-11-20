package anightdazingzoroark.riftlib.ui;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;

//another helper class, this one allows for quick transfer of data
//and conversion into nbt to transfer into a UI
public class RiftLibUIData {
    private final NBTTagCompound nbtTagCompound = new NBTTagCompound();

    public RiftLibUIData addInteger(String key, int data) {
        this.nbtTagCompound.setInteger(key, data);
        return this;
    }

    public RiftLibUIData addDouble(String key, double data) {
        this.nbtTagCompound.setDouble(key, data);
        return this;
    }

    public RiftLibUIData addFloat(String key, float data) {
        this.nbtTagCompound.setFloat(key, data);
        return this;
    }

    public RiftLibUIData addBoolean(String key, boolean data) {
        this.nbtTagCompound.setBoolean(key, data);
        return this;
    }

    public RiftLibUIData addString(String key, String data) {
        this.nbtTagCompound.setString(key, data);
        return this;
    }

    public RiftLibUIData addEntity(String key, Entity entity) {
        NBTTagCompound dataToAdd = new NBTTagCompound();
        entity.writeToNBTOptional(dataToAdd);
        this.nbtTagCompound.setTag(key, dataToAdd);
        return this;
    }

    public RiftLibUIData addNBTTagCompound(String key, NBTTagCompound data) {
        this.nbtTagCompound.setTag(key, data);
        return this;
    }

    //this combines an nbt tag compound with the one thats currently stored
    public RiftLibUIData parseNBT(NBTTagCompound nbtTagCompound) {
        this.nbtTagCompound.merge(nbtTagCompound);
        return this;
    }

    public NBTTagCompound getFinalNBT() {
        return this.nbtTagCompound;
    }
}
