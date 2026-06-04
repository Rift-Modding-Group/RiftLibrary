package anightdazingzoroark.riftlib.nbtStorageUser.propertyValue;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ListPropertyValue<I> extends AbstractPropertyValue<List<I>> {
    private final Function<List<I>, NBTBase> listWriter;
    private final Function<NBTBase, List<I>> listReader;

    public ListPropertyValue(
            String key,
            Function<List<I>, NBTBase> listWriter,
            Function<NBTBase, List<I>> listReader
    ) {
        super(key, new ArrayList<>());
        this.listWriter = listWriter;
        this.listReader = listReader;
    }

    public void add(I value) {
        this.value.add(value);
    }

    public void remove(I value) {
        this.value.remove(value);
    }

    public void clear() {
        this.value.clear();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setTag(this.getKey(), this.listWriter.apply(this.value));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.value = this.listReader.apply(nbtTagCompound.getTag(this.getKey()));
    }

    @Override
    public Class<List<I>> getHeldClass() {
        return (Class<List<I>>) (Class<?>) List.class;
    }
}
