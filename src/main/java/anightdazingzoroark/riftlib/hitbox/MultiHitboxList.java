package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class to manage collisionHitboxes on an IMultiHitboxUser instance
 * */
public class MultiHitboxList<T extends IMultiHitboxUser<?>> {
    @NotNull
    private final T multiHitboxUser;
    @NotNull
    private final AnimationDataEntity animData;
    private final Map<String, RiftLibCollisionHitbox<T>> collisionHitboxes = new HashMap<>();
    private final Map<String, RiftLibOffenseHitbox<T>> offenseHitboxes = new HashMap<>();
    private final Map<String, List<IHitbox<T>>> hitboxesByTag = new HashMap<>();

    public MultiHitboxList(@NotNull T multiHitboxUser, @NotNull AnimationDataEntity animData) {
        this.multiHitboxUser = multiHitboxUser;
        this.animData = animData;
    }

    /**
     * Ticked on server (and client hopefully) to update collisionHitboxes
     * */
    public void updateHitboxes() {
        //-----reset if bounding box list on anim data got recently updated-----
        if (this.animData.getBoundingBoxesRecentlyUpdated()) {
            this.hitboxesByTag.clear();
        }

        //-----create collision hitboxes from bounding boxes-----
        for (AnimatedBoundingBox animatedBoundingBox : this.animData.getAnimatedBoundingBoxes()) {
            if (!animatedBoundingBox.canDoCollisions()) continue;
            if (!this.collisionHitboxes.containsKey(animatedBoundingBox.getName())) {
                RiftLibCollisionHitbox<T> toCreate = new RiftLibCollisionHitbox<>(this.multiHitboxUser, animatedBoundingBox);

                this.collisionHitboxes.put(animatedBoundingBox.getName(), toCreate);
                this.hitboxesByTag.computeIfAbsent(animatedBoundingBox.getName(), key -> new ArrayList<>()).add(toCreate);
            }
        }

        //-----tick all already existing hitboxes-----
        for (RiftLibCollisionHitbox<T> collisionHitbox : this.collisionHitboxes.values()) collisionHitbox.onUpdate();
    }

    /**
     * Required because this is how proxy entities as collisionHitboxes are dealt with in vanilla
     * */
    public Entity[] getHitboxesAsArray() {
        return this.collisionHitboxes.values().toArray(Entity[]::new);
    }

    @NotNull
    public RiftLibCollisionHitbox<T> getCollisionHitboxByName(@NotNull String name) {
        IHitbox<T> hitbox = this.collisionHitboxes.get(name);
        if (!(hitbox instanceof RiftLibCollisionHitbox<T> collisionHitbox)) {
            throw new IllegalArgumentException("Given name " + name + " does not correspond to any collision hitbox on this entity!");
        }
        return collisionHitbox;
    }

    @NotNull
    public List<RiftLibCollisionHitbox<T>> getCollisionHitboxesByTag(@NotNull String tagName) {
        List<IHitbox<T>> hitboxList = this.hitboxesByTag.get(tagName);
        if (hitboxList == null) {
            throw new IllegalArgumentException("Given tag " + tagName + " does not correspond to any tags for collision collisionHitboxes on this entity!");
        }
        return hitboxList.stream().filter(hitbox -> hitbox instanceof RiftLibCollisionHitbox<T>)
                .map(new Function<IHitbox<T>, RiftLibCollisionHitbox<T>>() {
                    @Override
                    public RiftLibCollisionHitbox<T> apply(IHitbox<T> tiHitbox) {
                        return (RiftLibCollisionHitbox<T>) tiHitbox;
                    }
                }).toList();
    }
}
