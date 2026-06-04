package anightdazingzoroark.riftlib.nbtStorageUser.propertySystem;

import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.AbstractPropertyValue;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * To make a property, just extend this class. And to register it, use PropertyRegistry.register()
 * */
public abstract class AbstractEntityProperties<E extends Entity> {
    protected final Map<String, ImmutablePair<AbstractPropertyValue<?>, Boolean>> propertyValueMap = new HashMap<>();
    @NotNull
    private final String propertyName;
    private final E entityHolder;

    public AbstractEntityProperties(@NotNull String propertyName, @NotNull E entityHolder) {
        this.registerDefaults(entityHolder);
        this.propertyName = propertyName;
        this.entityHolder = entityHolder;
    }

    //-----initialization and registration-----
    /**
     * Override this to register any default values your property might have.
     * */
    protected abstract void registerDefaults(E entity);

    protected void register(AbstractPropertyValue<?> value) {
        this.register(value, true);
    }

    //persistence means that the data will be saved when the world is unloaded
    protected void register(AbstractPropertyValue<?> value, boolean persist) {
        //check if the property already exists, it it already does, skip
        if (this.propertyValueMap.containsKey(value.getKey())) {
            throw new UnsupportedOperationException("Key "+value.getKey()+" already exists in property "+this.getPropertyName()+"!");
        }

        //add the property
        this.propertyValueMap.put(value.getKey(), new ImmutablePair<>(value, persist));

        //sync to client afterwards from server
        this.syncToClient(value);
    }

    //-----methods relating to putting values-----
    //universal setter and getter
    @SuppressWarnings("unchecked")
    private <I extends AbstractPropertyValue<?>> I getExistingProperty(String key) {
        //check if key exists
        if (!this.propertyValueMap.containsKey(key)) {
            throw new UnsupportedOperationException("Key "+key+" does not exist in property "+this.getPropertyName()+"!");
        }

        return (I) this.propertyValueMap.get(key).left;
    }

    public <I> void set(String key, I value) {
        this.set(key, value, true);
    }

    public <I> void set(String key, I value, boolean includeSync) {
        //check if key corresponds to value
        AbstractPropertyValue<I> propertyValue = this.getExistingProperty(key);

        if (propertyValue.getHeldClass() != value.getClass()) {
            throw new UnsupportedOperationException("Key "+key+" does not represent given value!");
        }

        //now set as usual
        propertyValue.setValue(value);
        boolean persist = this.propertyValueMap.get(key).right;
        this.propertyValueMap.put(key, new ImmutablePair<>(propertyValue, persist));
        if (includeSync) this.syncToClient(propertyValue);
    }

    //sync to client from server
    private void syncToClient(AbstractPropertyValue<?> value) {
        if (this.entityHolder != null && !this.entityHolder.world.isRemote) {
            PropertiesNetworking.sendSingle(
                    this.entityHolder,
                    this.propertyName,
                    value.getKey(),
                    this.writeOneToNBT(value.getKey())
            );
        }
    }

    protected void syncToClientMultiple(String... keys) {
        if (this.entityHolder == null || this.entityHolder.world.isRemote) return;
        PropertiesNetworking.sendMultiple(
                this.entityHolder,
                this.propertyName,
                Arrays.asList(keys),
                this.writeMultipleToNBT(keys)
        );
    }

    //-----general getters-----
    public <I> I get(String key) {
        AbstractPropertyValue<I> propertyValue = this.getExistingProperty(key);
        return propertyValue.getValue();
    }

    public boolean has(String key) {
        return this.propertyValueMap.containsKey(key);
    }

    //-----holder related-----
    public @NotNull String getPropertyName() {
        return this.propertyName;
    }

    public @NotNull E getEntityHolder() {
        return this.entityHolder;
    }

    //-----nbt related stuff-----
    //save all properties
    public NBTTagCompound writeAllToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        for (ImmutablePair<AbstractPropertyValue<?>, Boolean> propertyValuePair : this.propertyValueMap.values()) {
            if (propertyValuePair.right) propertyValuePair.getLeft().writeToNBT(tag);
        }
        return tag;
    }

    //load all properties
    public void readAllFromNBT(NBTTagCompound tag) {
        for (ImmutablePair<AbstractPropertyValue<?>, Boolean> propertyValuePair : this.propertyValueMap.values()) {
            if (propertyValuePair.right) propertyValuePair.getLeft().readFromNBT(tag);
        }
    }

    //save one property by key (for delta sync)
    public NBTTagCompound writeOneToNBT(String key) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        ImmutablePair<AbstractPropertyValue<?>, Boolean> propertyValuePair = this.propertyValueMap.get(key);
        if (propertyValuePair == null) return nbtTagCompound;
        AbstractPropertyValue<?> propertyValue = propertyValuePair.getLeft();
        if (propertyValue != null) propertyValue.writeToNBT(nbtTagCompound);
        return nbtTagCompound;
    }

    //read one property by key (for delta sync)
    public void readOneFromNBT(NBTTagCompound tag, String key) {
        ImmutablePair<AbstractPropertyValue<?>, Boolean> propertyValuePair = this.propertyValueMap.get(key);
        if (propertyValuePair == null) return;
        AbstractPropertyValue<?> propertyValue = propertyValuePair.getLeft();
        if (propertyValue != null) propertyValue.readFromNBT(tag);
    }

    //save multiple specified properties by key
    public NBTTagCompound writeMultipleToNBT(String... keys) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        for (String key : keys) {
            ImmutablePair<AbstractPropertyValue<?>, Boolean> propertyValuePair = this.propertyValueMap.get(key);
            if (propertyValuePair == null) continue;
            AbstractPropertyValue<?> propertyValue = propertyValuePair.getLeft();
            if (propertyValue != null) propertyValue.writeToNBT(nbtTagCompound);
        }
        return nbtTagCompound;
    }

    //read multiple properties by key
    public void readMultipleFromNBT(NBTTagCompound tag, List<String> keys) {
        for (String key : keys) {
            ImmutablePair<AbstractPropertyValue<?>, Boolean> propertyValuePair = this.propertyValueMap.get(key);
            if (propertyValuePair == null) continue;
            AbstractPropertyValue<?> propertyValue = propertyValuePair.getLeft();
            if (propertyValue != null) propertyValue.readFromNBT(tag);
        }
    }
}
