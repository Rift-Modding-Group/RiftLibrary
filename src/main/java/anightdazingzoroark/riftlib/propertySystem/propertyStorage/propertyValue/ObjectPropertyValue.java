package anightdazingzoroark.riftlib.propertySystem.propertyStorage.propertyValue;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Function;

public class ObjectPropertyValue<O> extends AbstractPropertyValue<O> {
    private final Class<O> heldClass;
    private final Function<O, NBTBase> objectWriter;
    private final Function<NBTBase, O> objectReader;

    public ObjectPropertyValue(
            String key, O initValue, Class<O> heldClass,
            Function<O, NBTBase> objectWriter,
            Function<NBTBase, O> objectReader
    ) {
        super(key, initValue);
        this.heldClass = heldClass;
        this.objectWriter = objectWriter;
        this.objectReader = objectReader;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setTag(this.getKey(), this.objectWriter.apply(this.value));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.value = this.objectReader.apply(nbtTagCompound.getTag(this.getKey()));
    }

    public Class<O> getHeldClass() {
        return this.heldClass;
    }
}
