package anightdazingzoroark.riftlib.nbtStorageUser.propertyValue;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MapPropertyValue<K, V> extends AbstractPropertyValue<Map<K, V>> {
    private final Function<Map<K, V>, NBTBase> mapWriter;
    private final Function<NBTBase, Map<K, V>> mapReader;

    public MapPropertyValue(
            String key,
            Function<Map<K, V>, NBTBase> mapWriter,
            Function<NBTBase, Map<K, V>> mapReader
    ) {
        super(key, new HashMap<K, V>());
        this.mapWriter = mapWriter;
        this.mapReader = mapReader;
    }

    public void put(K mapKey, V mapValue) {
        this.value.put(mapKey, mapValue);
    }

    public void remove(K mapKey) {
        this.value.remove(mapKey);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setTag(this.getKey(), this.mapWriter.apply(this.value));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.value = this.mapReader.apply(nbtTagCompound.getTag(this.getKey()));
    }

    @Override
    public Class<Map<K, V>> getHeldClass() {
        return (Class<Map<K, V>>) (Class<?>) HashMap.class;
    }
}
