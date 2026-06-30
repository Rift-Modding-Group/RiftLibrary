package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class to manage hitboxes on an IMultiHitboxUser instance
 * */
public class MultiHitboxList<T extends IMultiHitboxUser<?>> {
    @NotNull
    private final T multiHitboxUser;
    @NotNull
    private final AnimationDataEntity animData;
    private final Map<String, IHitbox<T>> hitboxes = new HashMap<>();
    private final Map<String, List<IHitbox<T>>> hitboxesByTag = new HashMap<>();

    public MultiHitboxList(@NotNull T multiHitboxUser, @NotNull AnimationDataEntity animData) {
        this.multiHitboxUser = multiHitboxUser;
        this.animData = animData;
    }

    /**
     * Ticked on server (and client hopefully) to update hitboxes
     * */
    public void updateHitboxes() {

    }

    /**
     * Required because this is how proxy entities as hitboxes are dealt with in vanilla
     * */
    public Entity[] getHitboxesAsArray() {
        return this.hitboxes.values().stream().filter(hitbox -> hitbox instanceof RiftLibCollisionHitbox<T>).toArray(Entity[]::new);
    }

    @NotNull
    public RiftLibCollisionHitbox<T> getCollisionHitboxByName(@NotNull String name) {
        IHitbox<T> hitbox = this.hitboxes.get(name);
        if (!(hitbox instanceof RiftLibCollisionHitbox<T> collisionHitbox)) {
            throw new IllegalArgumentException("Given name " + name + " does not correspond to any collision hitbox on this entity!");
        }
        return collisionHitbox;
    }

    @NotNull
    public List<RiftLibCollisionHitbox<T>> getCollisionHitboxesByTag(@NotNull String tagName) {
        List<IHitbox<T>> hitboxList = this.hitboxesByTag.get(tagName);
        if (hitboxList == null) {
            throw new IllegalArgumentException("Given tag " + tagName + " does not correspond to any tags for collision hitboxes on this entity!");
        }
        //hehe
        return hitboxList.stream().filter(hitbox -> hitbox instanceof RiftLibCollisionHitbox<T>)
                .map(new Function<IHitbox<T>, RiftLibCollisionHitbox<T>>() {
                    @Override
                    public RiftLibCollisionHitbox<T> apply(IHitbox<T> tiHitbox) {
                        return (RiftLibCollisionHitbox<T>) tiHitbox;
                    }
                }).toList();
    }
}
