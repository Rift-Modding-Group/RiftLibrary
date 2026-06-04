package anightdazingzoroark.riftlib.propertySystem.registry;

import anightdazingzoroark.riftlib.propertySystem.propertyStorage.AbstractEntityProperties;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.Map;

public class PropertiesRoot {
    private final Map<String, AbstractEntityProperties<?>> sets = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <E extends Entity, T extends AbstractEntityProperties<?>> T getOrCreate(String key, E entity) {
        //get if it exists
        AbstractEntityProperties<?> existing = sets.get(key);
        if (existing != null) return (T) existing;

        //start creating if it doesnt
        PropertyRegistry.ClassPropertyPair<E> classPropertyPair = (PropertyRegistry.ClassPropertyPair<E>) PropertyRegistry.getPropertyClassPair(key, entity);
        if (classPropertyPair == null || classPropertyPair.entityClass() == null || classPropertyPair.propertyMaker() == null) return null;

        AbstractEntityProperties<?> created = classPropertyPair.propertyMaker().apply(key, entity);
        this.sets.put(key, created);
        return (T) created;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
        for (Map.Entry<String, AbstractEntityProperties<?>> entry : this.sets.entrySet()) {
            toReturn.setTag(entry.getKey(), entry.getValue().writeAllToNBT());
        }
        return toReturn;
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound, Entity entity) {
        for (String key : nbtTagCompound.getKeySet()) {
            NBTTagCompound setTag = nbtTagCompound.getCompoundTag(key);
            AbstractEntityProperties<?> set = this.getOrCreate(key, entity);
            if (set != null) set.readAllFromNBT(setTag);
        }
    }
}
