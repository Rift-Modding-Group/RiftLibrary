package anightdazingzoroark.riftlib.propertySystem;

import anightdazingzoroark.riftlib.propertySystem.propertyStorage.AbstractEntityProperties;
import anightdazingzoroark.riftlib.propertySystem.registry.PropertiesBootstrap;
import anightdazingzoroark.riftlib.propertySystem.registry.PropertiesRoot;
import net.minecraft.entity.Entity;

/**
 * Now why on earth would a GeckoLib fork add a (checks notes) a property system?
 * Well idk plus its convenient xd.
 * */
public class RiftLibProperty {
    public static <T extends AbstractEntityProperties<?>> T getProperty(String name, Entity entity) {
        PropertiesRoot root = entity.getCapability(PropertiesBootstrap.CAP, null);
        if (root == null) return null;
        return root.getOrCreate(name, entity);
    }
}
