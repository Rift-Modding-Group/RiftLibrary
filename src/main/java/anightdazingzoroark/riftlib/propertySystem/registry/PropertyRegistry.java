package anightdazingzoroark.riftlib.propertySystem.registry;

import anightdazingzoroark.riftlib.propertySystem.propertyStorage.AbstractEntityProperties;
import net.minecraft.entity.Entity;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Where all properties are stored.
 * */
public class PropertyRegistry {
    private static final Map<String, ClassPropertyPair<?>> REGISTRY = new HashMap<>();

    /**
     * Use this to register your property.
     * */
    public static void register(String name, ClassPropertyPair<?> propertyMaker) {
        REGISTRY.put(name, propertyMaker);
    }

    public static ClassPropertyPair<?> getPropertyClassPair(String key, Entity entity) {
        Class<? extends Entity> entityClass = entity.getClass();
        if (!REGISTRY.containsKey(key)) return null;
        ClassPropertyPair<?> classPropertyPair = REGISTRY.get(key);
        if (!classPropertyPair.entityClass.isAssignableFrom(entityClass)) return null;
        else return classPropertyPair;
    }

    public static Set<String> getAllPropertyNames() {
        return REGISTRY.keySet();
    }

    public static boolean entityCanHaveProperty(String key, Entity entity) {
        if (!REGISTRY.containsKey(key)) return false;
        ClassPropertyPair<?> classPropertyPair = REGISTRY.get(key);
        return classPropertyPair.entityClass.isAssignableFrom(entity.getClass());
    }

    //much shorter than declaring an ImmutablePair
    //and oh yeah, the bi function for property instead of a class removes
    //reflection related headaches. fuck reflection smh.
    public record ClassPropertyPair<E extends Entity>(Class<E> entityClass, BiFunction<String, E, AbstractEntityProperties<E>> propertyMaker) {}
}
